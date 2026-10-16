package com.sonicdevelopment.application.acceptance

import com.sonicdevelopment.application.acceptance.fixtures.FakeDrivenPorts
import com.sonicdevelopment.application.acceptance.fixtures.FakeHarborTest
import com.sonicdevelopment.application.acceptance.fixtures.STARTING_STOCK
import com.sonicdevelopment.application.acceptance.fixtures.SeedData
import com.sonicdevelopment.application.acceptance.fixtures.aShippingPublishedRecord
import com.sonicdevelopment.driving.adapter.messaging.ShippingEventListener
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestPropertySource
import java.util.*

/**
 * Feature: A ship arrives at its Destination Harbor.
 *
 * The Harbor is "Port Royal", on in-memory fakes. The Release at "Tortuga" is simulated by handing the listener
 * the `shipping-published` record Debezium would relay from Tortuga's outbox; the delivery is synchronous. The
 * seeded Catain "Catain Black Whiskers" stands in for "Whiskers".
 */
@FakeHarborTest
@TestPropertySource(properties = ["harbor.name=Port Royal"])
class ShipArrivesAtItsDestinationHarborAcceptanceTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var fakes: FakeDrivenPorts

    @Autowired
    lateinit var shippingEventListener: ShippingEventListener

    /** Background: "Black Pearl" with Catain "Whiskers", Released at "Tortuga" to "Port Royal" with "Rum" and "Silk". */
    private val blackPearlId: UUID = UUID.randomUUID()
    private val blackPearlShippingId: UUID = UUID.randomUUID()

    @BeforeEach
    fun resetHarbor() {
        fakes.reset()
    }

    @Test
    fun `The arrived ship joins the fleet of the Destination Harbor`() {
        // When "Black Pearl" arrives at "Port Royal"
        blackPearlIsAnnounced()

        // Then "Black Pearl" with Catain "Whiskers" and its Ship Id is among the Available Ships of "Port Royal"
        val ship = availableShips().single { it["id"] == blackPearlId.toString() }
        ship["name"] shouldBe "Black Pearl"
        ship["catain"] shouldBe WHISKERS
        // as an Incoming Ship, since it arrived with Cargo aboard (STORY-044)
        ship["incoming"] shouldBe true
        // And it has no Loaded Cargo and no Active Shipping
        (ship["shippingState"] == null || ship["shippingState"] == "IDLE") shouldBe true
        (shipDetails(blackPearlId)["cargo"] as List<*>).shouldBeEmpty()
    }

    @Test
    fun `The Destination Harbor announces the Arrival`() {
        // When "Black Pearl" arrives at "Port Royal"
        blackPearlIsAnnounced()

        // Then "Port Royal" announces Ship Arrived for "Black Pearl" to "Tortuga"
        val announced = fakes.outbox.shipArrived()
        announced shouldHaveSize 1
        val arrived = announced.single()
        arrived.shippingId shouldBe blackPearlShippingId
        arrived.shipId shouldBe blackPearlId
        arrived.shipName shouldBe "Black Pearl"
        arrived.originHarbor shouldBe "Tortuga"
        arrived.destinationHarbor shouldBe "Port Royal"
    }

    @Test
    fun `An Arrival announced twice is handled once`() {
        // Given "Black Pearl" has already arrived at "Port Royal"
        val stockBeforeArrival = stock()
        val firstAnnouncement = blackPearlIsAnnounced()

        // When the Release of "Black Pearl" is announced to "Port Royal" again:
        // redelivered with the same event id, and re-published under a new one
        blackPearlIsAnnounced(eventId = firstAnnouncement)
        blackPearlIsAnnounced()

        // Then the Stock of "Port Royal" is unchanged
        stock() shouldBe stockBeforeArrival
        // And "Black Pearl" appears exactly once among the Available Ships of "Port Royal"
        availableShips().count { it["id"] == blackPearlId.toString() } shouldBe 1
        // And once among its Incoming Ships, with its "Rum" and "Silk" aboard once each
        val incoming = incomingShips().filter { it["shipId"] == blackPearlId.toString() }
        incoming shouldHaveSize 1
        cargoNames(incoming.single()).sorted() shouldBe listOf("Rum", "Silk")
        fakes.outbox.shipArrived() shouldHaveSize 1
    }

    @Test
    fun `A Harbor ignores ships sailing to another Harbor`() {
        // Given the ship "Flying Dutchman" was Released at "Tortuga" to "Nassau"
        val flyingDutchman = aShippingPublishedRecord(
            shipName = "Flying Dutchman",
            catainId = SeedData.catainIdOf(WHISKERS),
            cargoIds = listOf(SeedData.cargoIdOf("Rum")),
            originHarbor = "Tortuga",
            destinationHarbor = "Nassau",
        )

        // When the Release of "Flying Dutchman" is announced to "Port Royal"
        shippingEventListener.onShippingEvent(flyingDutchman)

        // Then the Stock and the fleet of "Port Royal" are unchanged
        stock().values.forEach { it shouldBe STARTING_STOCK }
        availableShips().shouldBeEmpty()
        incomingShips().shouldBeEmpty()
        fakes.outbox.shipArrived().shouldBeEmpty()
    }

    /** Hands the listener the Shipping Published of "Black Pearl" for "Port Royal" and returns its event id. */
    private fun blackPearlIsAnnounced(eventId: UUID = UUID.randomUUID()): UUID {
        shippingEventListener.onShippingEvent(
            aShippingPublishedRecord(
                shipId = blackPearlId,
                shipName = "Black Pearl",
                catainId = SeedData.catainIdOf(WHISKERS),
                shippingId = blackPearlShippingId,
                cargoIds = listOf(SeedData.cargoIdOf("Rum"), SeedData.cargoIdOf("Silk")),
                originHarbor = "Tortuga",
                destinationHarbor = "Port Royal",
                eventId = eventId,
            )
        )
        return eventId
    }

    /** The Stock as the User sees it among the Available Cargo; a Cargo out of Stock counts 0. */
    private fun stock(): Map<String, Int> {
        val response = restTemplate.getForEntity("/web/cargos", List::class.java)
        response.statusCode shouldBe HttpStatus.OK
        val offered = response.body!!.map { it as Map<*, *> }.associate { it["name"] as String to it["stock"] as Int }
        return SeedData.allCargo().map { it.name }.associateWith { offered[it] ?: 0 }
    }

    private fun availableShips(): List<Map<*, *>> {
        val response = restTemplate.getForEntity("/web/ships", List::class.java)
        response.statusCode shouldBe HttpStatus.OK
        return response.body!!.map { it as Map<*, *> }
    }

    private fun incomingShips(): List<Map<*, *>> {
        val response = restTemplate.getForEntity("/web/incoming-ships", List::class.java)
        response.statusCode shouldBe HttpStatus.OK
        return response.body!!.map { it as Map<*, *> }
    }

    private fun cargoNames(incomingShip: Map<*, *>): List<String> =
        (incomingShip["cargo"] as List<*>).map { (it as Map<*, *>)["name"] as String }

    private fun shipDetails(shipId: UUID): Map<*, *> {
        val response = restTemplate.getForEntity("/web/ships/$shipId", Map::class.java)
        response.statusCode shouldBe HttpStatus.OK
        return response.body!!
    }

    private companion object {
        const val WHISKERS = "Catain Black Whiskers"
    }
}
