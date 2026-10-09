package com.sonicdevelopment.application.acceptance

import com.fasterxml.jackson.databind.ObjectMapper
import com.sonicdevelopment.application.acceptance.fixtures.FakeDrivenPorts
import com.sonicdevelopment.application.acceptance.fixtures.FakeHarborTest
import com.sonicdevelopment.application.acceptance.fixtures.SeedData
import com.sonicdevelopment.application.acceptance.fixtures.aShippingPublishedRecord
import com.sonicdevelopment.application.acceptance.fixtures.givenPriceOf
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
 * Feature: An arriving ship becomes an Incoming Ship.
 *
 * The Harbor is "Port Royal", on in-memory fakes. The Release at "Tortuga" is simulated by handing the listener
 * the `shipping-published` record Debezium would relay; the delivery is synchronous. State is read over HTTP:
 * `GET /web/ships`, `GET /web/incoming-ships` (Delivery Price as a decimal string) and `GET /web/stock`.
 *
 * Background: the Price of Rum is 40.00 and of Sugar 35.00 here; "Salty Whisker" was Released to this Harbor
 * with 2 Rum and 1 Sugar aboard.
 */
@FakeHarborTest
@TestPropertySource(properties = ["harbor.name=Port Royal"])
class AnArrivingShipBecomesAnIncomingShipAcceptanceTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var fakes: FakeDrivenPorts

    @Autowired
    lateinit var shippingEventListener: ShippingEventListener

    private val saltyWhiskerId: UUID = UUID.randomUUID()
    private val saltyWhiskerShippingId: UUID = UUID.randomUUID()

    @BeforeEach
    fun resetHarbor() {
        fakes.reset()
        fakes.givenPriceOf("Rum", "40.00")
        fakes.givenPriceOf("Sugar", "35.00")
    }

    @Test
    fun `An arriving ship becomes an Incoming Ship with its Cargo aboard`() {
        val stockBefore = stock()

        // When "Salty Whisker" arrives
        saltyWhiskerArrives()

        // Then "Salty Whisker" is in the fleet as an Incoming Ship
        val ship = ships().single { it["id"] == saltyWhiskerId.toString() }
        ship["name"] shouldBe "Salty Whisker"
        ship["incoming"] shouldBe true
        // And its 2 Rum and 1 Sugar are still aboard
        val incoming = incomingShips().single { it["shipId"] == saltyWhiskerId.toString() }
        cargoNames(incoming).sorted() shouldBe listOf("Rum", "Rum", "Sugar")
        // And the Stock is unchanged
        stock() shouldBe stockBefore
        // And the Origin Harbor is told that the voyage is over
        val announced = fakes.outbox.shipArrived()
        announced shouldHaveSize 1
        announced.single().shippingId shouldBe saltyWhiskerShippingId
        announced.single().originHarbor shouldBe "Tortuga"
    }

    @Test
    fun `A ship arriving with no Cargo joins the fleet as an ordinary ship`() {
        // Given the ship "Empty Tabby" was Released to this Harbor with no Cargo aboard
        val emptyTabbyId = UUID.randomUUID()
        val emptyTabbyShippingId = UUID.randomUUID()

        // When "Empty Tabby" arrives
        shippingEventListener.onShippingEvent(
            aShippingPublishedRecord(
                shipId = emptyTabbyId,
                shipName = "Empty Tabby",
                catainId = SeedData.aCatainId,
                shippingId = emptyTabbyShippingId,
                cargoIds = emptyList(),
                originHarbor = "Tortuga",
                destinationHarbor = "Port Royal",
            )
        )

        // Then "Empty Tabby" is in the fleet
        val ship = ships().single { it["id"] == emptyTabbyId.toString() }
        // And "Empty Tabby" is not an Incoming Ship
        ship["incoming"] shouldBe false
        incomingShips().shouldBeEmpty()
        // And the Origin Harbor is told that the voyage is over
        val announced = fakes.outbox.shipArrived()
        announced shouldHaveSize 1
        announced.single().shippingId shouldBe emptyTabbyShippingId
        announced.single().originHarbor shouldBe "Tortuga"
    }

    @Test
    fun `Incoming Ships are listed on the harbor management page`() {
        // Given "Salty Whisker" has arrived
        saltyWhiskerArrives()

        // When the User opens the harbor management page
        val incoming = incomingShips()

        // Then "Salty Whisker" is listed as an Incoming Ship with a Delivery Price of 115.00 $
        incoming shouldHaveSize 1
        incoming.single()["name"] shouldBe "Salty Whisker"
        incoming.single()["deliveryPrice"] shouldBe "115.00"
    }

    @Test
    fun `An Incoming Ship cannot get a new Shipping`() {
        // Given "Salty Whisker" has arrived
        saltyWhiskerArrives()

        // When the User starts a new Shipping for "Salty Whisker"
        val response = restTemplate.postForEntity("/web/ships/$saltyWhiskerId/shippings", null, Map::class.java)

        // Then the User is told that "Salty Whisker" must be unloaded or refused first
        response.statusCode shouldBe HttpStatus.CONFLICT
        response.body!!["detail"] shouldBe "Salty Whisker must be unloaded or refused first"
    }

    @Test
    fun `The Delivery Price follows this Harbor's Prices`() {
        // Given the Price of Rum is 55.00 $ at the Origin Harbor of "Salty Whisker":
        // not expressible on one Harbor. Shipping Published carries no Prices, so the Origin's 55.00 $
        // cannot reach this Harbor; the Prices of Port Royal (Background) are the only ones there are.
        // And "Salty Whisker" has arrived
        saltyWhiskerArrives()

        // When the User opens the harbor management page
        val incoming = incomingShips()

        // Then "Salty Whisker" is listed as an Incoming Ship with a Delivery Price of 115.00 $
        incoming.single { it["name"] == "Salty Whisker" }["deliveryPrice"] shouldBe "115.00"
    }

    private fun saltyWhiskerArrives() {
        val rum = SeedData.cargoIdOf("Rum")
        val sugar = SeedData.cargoIdOf("Sugar")
        shippingEventListener.onShippingEvent(
            aShippingPublishedRecord(
                shipId = saltyWhiskerId,
                shipName = "Salty Whisker",
                catainId = SeedData.aCatainId,
                shippingId = saltyWhiskerShippingId,
                cargoIds = listOf(rum, rum, sugar),
                originHarbor = "Tortuga",
                destinationHarbor = "Port Royal",
            )
        )
    }

    private fun cargoNames(incomingShip: Map<*, *>): List<String> =
        (incomingShip["cargo"] as List<*>).map { (it as Map<*, *>)["name"] as String }

    private fun ships(): List<Map<*, *>> = getList("/web/ships")

    private fun incomingShips(): List<Map<*, *>> = getList("/web/incoming-ships")

    /** The Stock as `GET /web/stock` shows it: quantity by Cargo name. */
    private fun stock(): Map<String, Int> =
        getList("/web/stock").associate { it["name"] as String to it["quantity"] as Int }

    private fun getList(url: String): List<Map<*, *>> {
        // read as text first: a missing endpoint answers a Problem Detail object, which must fail on the status
        val response = restTemplate.getForEntity(url, String::class.java)
        response.statusCode shouldBe HttpStatus.OK
        return ObjectMapper().readValue(response.body!!, List::class.java).map { it as Map<*, *> }
    }
}
