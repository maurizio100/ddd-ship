package com.sonicdevelopment.application.acceptance

import com.fasterxml.jackson.databind.ObjectMapper
import com.sonicdevelopment.application.acceptance.fixtures.FakeDrivenPorts
import com.sonicdevelopment.application.acceptance.fixtures.FakeHarborTest
import com.sonicdevelopment.application.acceptance.fixtures.SeedData
import com.sonicdevelopment.application.acceptance.fixtures.aShippingPublishedRecord
import com.sonicdevelopment.application.acceptance.fixtures.givenKnownHarbors
import com.sonicdevelopment.application.acceptance.fixtures.release
import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.values.CatainId
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort
import com.sonicdevelopment.driving.adapter.messaging.ShippingEventListener
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestPropertySource
import java.util.*

/**
 * Feature: A ship has a Ship Class.
 *
 * One Harbor ("Port Royal") on in-memory fakes. Arrival at another Harbor is simulated by handing the listener the
 * `shipping-published` record Debezium would relay. State is read over HTTP: `GET /web/ships`, `GET /web/ships/{id}`.
 */
@FakeHarborTest
@TestPropertySource(properties = ["harbor.name=Port Royal"])
class AShipHasAShipClassAcceptanceTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var fakes: FakeDrivenPorts

    @Autowired
    lateinit var shippingEventListener: ShippingEventListener

    @BeforeEach
    fun aFreshHarbor() {
        fakes.reset()
    }

    @ParameterizedTest(name = "{0} has the Crew Capacity {1}")
    @CsvSource("SLOOP,2", "BRIG,4", "GALLEON,6")
    fun `The Ship Class sets the Crew Capacity`(shipClass: String, capacity: Int) {
        // When the User creates the ship "Salty Whisker" as a "<ship class>"
        val id = createSaltyWhisker(shipClass)

        // Then the Crew Capacity of "Salty Whisker" is <capacity>
        fleetEntryOf(id)["shipClass"] shouldBe shipClass
        fleetEntryOf(id)["crewCapacity"] shouldBe capacity
    }

    @Test
    fun `A ship cannot be created without a Ship Class`() {
        // When the User creates the ship "Salty Whisker" without choosing a Ship Class
        val response = restTemplate.postForEntity(
            "/web/ships", mapOf("name" to "Salty Whisker", "catainId" to SeedData.aCatainId), Map::class.java
        )

        // Then the ship is not created
        response.statusCode shouldBe HttpStatus.BAD_REQUEST
        fleet() shouldBe emptyList<Any>()

        // And the User is told to choose a Ship Class
        (response.body!!["detail"] as String) shouldContain "Choose a Ship Class"
    }

    @Test
    fun `Available Ships show each ship's Ship Class`() {
        // Given "Salty Whisker" is a Brig
        val id = createSaltyWhisker("BRIG")

        // When the User looks at the Available Ships
        // Then "Salty Whisker" is shown as a Brig
        fleetEntryOf(id)["name"] shouldBe "Salty Whisker"
        fleetEntryOf(id)["shipClass"] shouldBe "BRIG"
    }

    @Test
    fun `Ships registered before Ship Classes existed are Sloops`() {
        // Given the ship "Old Barnacle" was registered before Ship Classes existed
        val id = UUID.randomUUID()
        fakes.fleet.saveNewShip(
            ShipRepositoryPort.InitialShipInformation.fromShip(
                Ship(
                    id = com.sonicdevelopment.domain.model.values.ShipId(id),
                    name = "Old Barnacle",
                    catainId = CatainId(SeedData.aCatainId),
                    catainName = "Catain",
                    homeHarbor = HarborName("Port Royal"),
                )
            )
        )

        // When the User looks at the Available Ships
        // Then "Old Barnacle" is shown as a Sloop
        fleetEntryOf(id)["name"] shouldBe "Old Barnacle"
        fleetEntryOf(id)["shipClass"] shouldBe "SLOOP"
        fleetEntryOf(id)["crewCapacity"] shouldBe 2
    }

    @Test
    fun `The Max Weight is the same for every Ship Class`() {
        // Given "Salty Whisker" is a Galleon
        val id = createSaltyWhisker("GALLEON")
        fleetEntryOf(id)["shipClass"] shouldBe "GALLEON"

        // When the User loads Cargo onto "Salty Whisker"
        restTemplate.postForEntity("/web/ships/$id/shippings", null, Map::class.java).statusCode shouldBe HttpStatus.OK
        restTemplate.postForEntity(
            "/web/ships/$id/cargos", mapOf("cargoId" to SeedData.cargoIdOf("Rum")), Map::class.java
        ).statusCode shouldBe HttpStatus.OK

        // Then the Max Weight of "Salty Whisker" is 15.0
        val details = restTemplate.getForEntity("/web/ships/$id", Map::class.java)
        details.statusCode shouldBe HttpStatus.OK
        (details.body!!["maxweight"] as Number).toDouble() shouldBe 15.0
    }

    @Test
    fun `A ship keeps its Ship Class at another Harbor`() {
        // Given "Salty Whisker" is a Brig at "Port Royal" and is Released to "Tortuga"
        fakes.givenKnownHarbors("Tortuga")
        val id = createSaltyWhisker("BRIG")
        restTemplate.postForEntity("/web/ships/$id/shippings", null, Map::class.java).statusCode shouldBe HttpStatus.OK
        restTemplate.release(id, "Tortuga").statusCode shouldBe HttpStatus.OK

        // The Shipping Published it sails with carries the Ship Class
        fakes.outbox.shippingPublished().single().shipClass shouldBe "BRIG"

        // When "Salty Whisker" arrives at "Tortuga" (here, the same event reaches a Harbor as "Port Royal")
        val arrivingId = UUID.randomUUID()
        shippingEventListener.onShippingEvent(
            aShippingPublishedRecord(
                shipId = arrivingId,
                shipName = "Salty Whisker",
                catainId = SeedData.aCatainId,
                originHarbor = "Tortuga",
                destinationHarbor = "Port Royal",
                homeHarbor = "Tortuga",
                shipClass = "BRIG",
            )
        )

        // Then "Salty Whisker" is shown as a Brig
        fleetEntryOf(arrivingId)["shipClass"] shouldBe "BRIG"
        fleetEntryOf(arrivingId)["crewCapacity"] shouldBe 4
    }

    @Test
    fun `A ship from a Harbor that does not send the Ship Class arrives as a Sloop`() {
        // When a ship arrives in an event without a Ship Class
        val arrivingId = UUID.randomUUID()
        shippingEventListener.onShippingEvent(
            aShippingPublishedRecord(
                shipId = arrivingId,
                shipName = "Salty Whisker",
                catainId = SeedData.aCatainId,
                originHarbor = "Tortuga",
                destinationHarbor = "Port Royal",
            )
        )

        // Then it is a Sloop
        fleetEntryOf(arrivingId)["shipClass"] shouldBe "SLOOP"
        fleetEntryOf(arrivingId)["crewCapacity"] shouldBe 2
    }

    private fun createSaltyWhisker(shipClass: String): UUID {
        val created = restTemplate.postForEntity(
            "/web/ships",
            mapOf("name" to "Salty Whisker", "catainId" to SeedData.aCatainId, "shipClass" to shipClass),
            Map::class.java,
        )
        created.statusCode shouldBe HttpStatus.OK
        return UUID.fromString(created.body!!["id"] as String)
    }

    private fun fleet(): List<Map<*, *>> {
        val response = restTemplate.getForEntity("/web/ships", String::class.java)
        response.statusCode shouldBe HttpStatus.OK
        return ObjectMapper().readValue(response.body!!, List::class.java).map { it as Map<*, *> }
    }

    private fun fleetEntryOf(shipId: UUID): Map<*, *> = fleet().single { it["id"] == shipId.toString() }
}
