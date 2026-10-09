package com.sonicdevelopment.application.acceptance

import com.sonicdevelopment.application.acceptance.fixtures.FakeDrivenPorts
import com.sonicdevelopment.application.acceptance.fixtures.FakeHarborTest
import com.sonicdevelopment.application.acceptance.fixtures.FleetEvent
import com.sonicdevelopment.application.acceptance.fixtures.FleetEventStream
import com.sonicdevelopment.application.acceptance.fixtures.SeedData
import com.sonicdevelopment.application.acceptance.fixtures.aShipArrivedRecord
import com.sonicdevelopment.application.acceptance.fixtures.aShipBeingPrepared
import com.sonicdevelopment.application.acceptance.fixtures.aShippingPublishedRecord
import com.sonicdevelopment.application.acceptance.fixtures.givenKnownHarbors
import com.sonicdevelopment.application.acceptance.fixtures.release
import com.sonicdevelopment.driving.adapter.messaging.ShippingEventListener
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestPropertySource
import java.util.*

/**
 * Feature: A Harbor sees ships arrive and leave as they happen.
 *
 * "The User is looking at the fleet" means the User's browser has the fleet-events stream
 * (`GET /web/fleet-events`) open, as the Available Ships page does; what it is pushed is what the User sees
 * change. Arrivals and Ship Arrived are simulated by handing the listener the records Debezium would relay from
 * the other Harbor's outbox; the delivery is synchronous.
 */
class AHarborSeesShipsArriveAndLeaveAcceptanceTest {

    @Nested
    @TestPropertySource(properties = ["harbor.name=Port Royal"])
    inner class AtPortRoyal : AHarbor() {

        @Test
        fun `An arriving ship appears without a reload`() {
            // Given the User at "Port Royal" is looking at the fleet
            val blackPearlId = UUID.randomUUID()

            // When "Black Pearl" arrives at "Port Royal" from "Tortuga"
            shippingEventListener.onShippingEvent(
                aShippingPublishedRecord(
                    shipId = blackPearlId,
                    shipName = "Black Pearl",
                    catainId = SeedData.aCatainId,
                    originHarbor = "Tortuga",
                    destinationHarbor = "Port Royal",
                )
            )

            // Then the User is told that "Black Pearl" arrived from "Tortuga"
            val pushed = fleet.nextAbout("ship-arrived", blackPearlId)
            pushed.data["shipName"] shouldBe "Black Pearl"
            pushed.data["originHarbor"] shouldBe "Tortuga"
            // And "Black Pearl" appears among the Available Ships of "Port Royal": the push came after the commit,
            // so the refetch it triggers already lists it
            availableShips().single { it["id"] == blackPearlId.toString() }["name"] shouldBe "Black Pearl"
        }

        @Test
        fun `Ships sailing to another Harbor change nothing`() {
            // Given the User at "Port Royal" is looking at the fleet

            // When "Flying Dutchman" arrives at "Nassau"
            val flyingDutchman = aShippingPublishedRecord(
                shipName = "Flying Dutchman",
                catainId = SeedData.aCatainId,
                originHarbor = "Tortuga",
                destinationHarbor = "Nassau",
            )
            shippingEventListener.onShippingEvent(flyingDutchman)

            // Then the fleet of "Port Royal" is unchanged
            availableShips().shouldBeEmpty()

            // And the User is told nothing: the deliveries are synchronous, so the first Arrival pushed after both
            // is Black Pearl's
            val blackPearlId = UUID.randomUUID()
            shippingEventListener.onShippingEvent(
                aShippingPublishedRecord(
                    shipId = blackPearlId,
                    shipName = "Black Pearl",
                    catainId = SeedData.aCatainId,
                    destinationHarbor = "Port Royal",
                )
            )
            val told = fleet.allUntil { it.name == "ship-arrived" && it.data["shipId"] == blackPearlId.toString() }
            told.none { it.data["shipName"] == "Flying Dutchman" } shouldBe true
            told.filter { it.name == "ship-arrived" }.map { it.data["shipName"] } shouldBe listOf("Black Pearl")
        }
    }

    @Nested
    @TestPropertySource(properties = ["harbor.name=Tortuga"])
    inner class AtTortuga : AHarbor() {

        @Test
        fun `A ship that arrived elsewhere leaves the fleet without a reload`() {
            // Given the User at "Tortuga" is looking at the fleet
            // And "Black Pearl" is at sea from "Tortuga" to "Port Royal"
            fakes.givenKnownHarbors("Port Royal")
            val blackPearlId = restTemplate.aShipBeingPrepared("Black Pearl")
            val release = restTemplate.release(blackPearlId, "Port Royal")
            release.statusCode shouldBe HttpStatus.OK

            // When "Tortuga" learns that "Black Pearl" arrived at "Port Royal"
            shippingEventListener.onShippingEvent(
                aShipArrivedRecord(
                    shipId = blackPearlId,
                    shipName = "Black Pearl",
                    shippingId = UUID.fromString(release.body!!["id"] as String),
                    originHarbor = "Tortuga",
                    destinationHarbor = "Port Royal",
                )
            )

            // Then "Black Pearl" is no longer among the Available Ships of "Tortuga"
            val pushed = fleet.nextAbout("ship-left", blackPearlId)
            pushed.data["shipName"] shouldBe "Black Pearl"
            pushed.data["destinationHarbor"] shouldBe "Port Royal"
            availableShips().none { it["id"] == blackPearlId.toString() } shouldBe true
        }
    }

    /** One Harbor on in-memory fakes with a User looking at its fleet. */
    @FakeHarborTest
    abstract class AHarbor {

        @Autowired
        lateinit var restTemplate: TestRestTemplate

        @Autowired
        lateinit var fakes: FakeDrivenPorts

        @Autowired
        lateinit var shippingEventListener: ShippingEventListener

        @LocalServerPort
        var port: Int = 0

        lateinit var fleet: FleetEventStream

        @BeforeEach
        fun aUserIsLookingAtTheFleet() {
            fakes.reset()
            fleet = FleetEventStream.open("http://localhost:$port")
        }

        @AfterEach
        fun theUserLooksAway() {
            fleet.close()
        }

        fun availableShips(): List<Map<*, *>> {
            val response = restTemplate.getForEntity("/web/ships", List::class.java)
            response.statusCode shouldBe HttpStatus.OK
            return response.body!!.map { it as Map<*, *> }
        }

        /** The first pushed event named [name] about the ship [shipId]; fails if it never comes. */
        fun FleetEventStream.nextAbout(name: String, shipId: UUID): FleetEvent =
            allUntil { it.name == name && it.data["shipId"] == shipId.toString() }.last()

        /** Every pushed event up to and including the first that matches [last]; fails if it never comes. */
        fun FleetEventStream.allUntil(last: (FleetEvent) -> Boolean): List<FleetEvent> {
            val seen = mutableListOf<FleetEvent>()
            while (true) {
                val event = next().shouldNotBeNull()
                seen += event
                if (last(event)) return seen
            }
        }
    }
}
