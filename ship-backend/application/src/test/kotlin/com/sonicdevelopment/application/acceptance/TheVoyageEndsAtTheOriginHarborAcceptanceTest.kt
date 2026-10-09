package com.sonicdevelopment.application.acceptance

import com.sonicdevelopment.application.acceptance.fixtures.FakeDrivenPorts
import com.sonicdevelopment.application.acceptance.fixtures.FakeHarborTest
import com.sonicdevelopment.application.acceptance.fixtures.SeedData
import com.sonicdevelopment.application.acceptance.fixtures.aShipArrivedRecord
import com.sonicdevelopment.application.acceptance.fixtures.aShipBeingPrepared
import com.sonicdevelopment.application.acceptance.fixtures.aShippingPublishedRecord
import com.sonicdevelopment.application.acceptance.fixtures.givenKnownHarbors
import com.sonicdevelopment.application.acceptance.fixtures.release
import com.sonicdevelopment.domain.model.values.CargoId
import com.sonicdevelopment.driving.adapter.messaging.ShippingEventListener
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestPropertySource
import java.util.*

/**
 * Feature: The voyage ends at the Origin Harbor.
 *
 * At "Tortuga" the Release is real (over HTTP); the Arrival at "Port Royal" is simulated by handing the
 * listener the `ship-arrived` record Debezium would relay from Port Royal's outbox. At "Port Royal" the Arrival
 * itself is simulated by handing it Tortuga's `shipping-published`. The delivery is synchronous, so the Harbor
 * has learned of an event when the call returns.
 */
class TheVoyageEndsAtTheOriginHarborAcceptanceTest {

    @Nested
    @TestPropertySource(properties = ["harbor.name=Tortuga"])
    inner class AtTortuga : AHarbor() {

        lateinit var blackPearlId: UUID
        lateinit var blackPearlShippingId: UUID

        /** Background: "Black Pearl" was Released at "Tortuga" to "Port Royal". */
        @BeforeEach
        fun blackPearlIsReleasedToPortRoyal() {
            fakes.givenKnownHarbors("Port Royal")
            blackPearlId = restTemplate.aShipBeingPrepared("Black Pearl")
            val release = restTemplate.release(blackPearlId, "Port Royal")
            release.statusCode shouldBe HttpStatus.OK
            blackPearlShippingId = UUID.fromString(release.body!!["id"] as String)
        }

        @Test
        fun `The ship stays at sea until it has arrived`() {
            // Then "Tortuga" lists "Black Pearl" as at sea
            val ship = availableShips().single { it["id"] == blackPearlId.toString() }
            ship["name"] shouldBe "Black Pearl"
            ship["shippingState"] shouldBe "SHIPPING"
        }

        @Test
        fun `The Shipping is done once the ship has arrived`() {
            // When "Tortuga" learns that "Black Pearl" arrived at "Port Royal"
            blackPearlArrivedAtPortRoyal()

            // Then the Shipping of "Black Pearl" is done
            shippingStateOfBlackPearlsVoyage() shouldBe "DONE"
            // And "Black Pearl" is no longer among the Available Ships of "Tortuga"
            availableShips().none { it["id"] == blackPearlId.toString() } shouldBe true
            restTemplate.getForEntity("/web/ships/$blackPearlId", Map::class.java).statusCode shouldBe
                HttpStatus.NOT_FOUND
        }

        @Test
        fun `An Arrival reported twice is handled once`() {
            // Given "Tortuga" has learned that "Black Pearl" arrived at "Port Royal"
            val firstReport = blackPearlArrivedAtPortRoyal()

            // When "Tortuga" learns it again: redelivered with the same event id, and re-published under a new one
            blackPearlArrivedAtPortRoyal(eventId = firstReport)
            val republished = blackPearlArrivedAtPortRoyal()

            // Then the Shipping of "Black Pearl" is still done
            shippingStateOfBlackPearlsVoyage() shouldBe "DONE"
            availableShips().none { it["id"] == blackPearlId.toString() } shouldBe true
            // And neither report was left failing: both are recorded as consumed
            fakes.inbox.hasConsumed(firstReport) shouldBe true
            fakes.inbox.hasConsumed(republished) shouldBe true
        }

        @Test
        fun `A ship that left the fleet is taken in again when it returns`() {
            // Given "Black Pearl" has arrived at "Port Royal" and "Tortuga" has learned of it
            blackPearlArrivedAtPortRoyal()

            // When "Black Pearl" is Released at "Port Royal" back to "Tortuga" and arrives
            val returnRelease = aShippingPublishedRecord(
                shipId = blackPearlId,
                shipName = "Black Pearl",
                catainId = SeedData.aCatainId,
                shippingId = UUID.randomUUID(),
                originHarbor = "Port Royal",
                destinationHarbor = "Tortuga",
            )
            shippingEventListener.onShippingEvent(returnRelease)

            // Then "Black Pearl" appears exactly once among the Available Ships, with no Active Shipping
            val ships = availableShips().filter { it["id"] == blackPearlId.toString() }
            ships.size shouldBe 1
            ships.single()["shippingState"] shouldBe "IDLE"
            // And its first Shipping is still readable as done
            shippingStateOfBlackPearlsVoyage() shouldBe "DONE"

            // And it can sail again from "Tortuga"
            restTemplate.postForEntity("/web/ships/$blackPearlId/shippings", null, Map::class.java)
                .statusCode shouldBe HttpStatus.OK
            val nextRelease = restTemplate.release(blackPearlId, "Port Royal")
            nextRelease.statusCode shouldBe HttpStatus.OK
            nextRelease.body!!["id"] shouldNotBe blackPearlShippingId.toString()
            availableShips().single { it["id"] == blackPearlId.toString() }["shippingState"] shouldBe "SHIPPING"
            shippingStateOfBlackPearlsVoyage() shouldBe "DONE"
        }

        @Test
        fun `A ship that left the fleet cannot be deleted`() {
            // Given "Tortuga" has learned that "Black Pearl" arrived at "Port Royal"
            blackPearlArrivedAtPortRoyal()

            // When the User at "Tortuga" deletes "Black Pearl"
            val response = restTemplate.exchange(
                "/web/ships/$blackPearlId", HttpMethod.DELETE, null, String::class.java
            )

            // Then it is not found, like any ship that is not in the fleet
            response.statusCode shouldBe HttpStatus.NOT_FOUND
            // And its record and its done Shipping are kept: the Shipping can only be read through the ship record
            shippingStateOfBlackPearlsVoyage() shouldBe "DONE"
        }

        @Test
        fun `A ship returning before Tortuga has learned of its Arrival elsewhere stays in the fleet`() {
            // Given "Black Pearl" arrived at "Port Royal", sailed on to "Nassau" and was Released there back
            // to "Tortuga", and that Release reaches "Tortuga" before Port Royal's Ship Arrived (arc42 R-10)
            val rumBefore = rumInStock()
            val returnShippingId = UUID.randomUUID()
            val returnRelease = aShippingPublishedRecord(
                shipId = blackPearlId,
                shipName = "Black Pearl",
                catainId = SeedData.aCatainId,
                shippingId = returnShippingId,
                cargoIds = listOf(SeedData.cargoIdOf("Rum")),
                originHarbor = "Nassau",
                destinationHarbor = "Tortuga",
            )

            // When "Black Pearl" arrives at "Tortuga", and only then "Tortuga" learns of its Arrival at "Port Royal"
            shippingEventListener.onShippingEvent(returnRelease)
            blackPearlArrivedAtPortRoyal()

            // Then "Black Pearl" is among the Available Ships of "Tortuga" once, with no Active Shipping
            val ships = availableShips().filter { it["id"] == blackPearlId.toString() }
            ships.size shouldBe 1
            ships.single()["shippingState"] shouldBe "IDLE"
            // And its voyage to "Port Royal" is done
            shippingStateOfBlackPearlsVoyage() shouldBe "DONE"
            // And its Cargo was unloaded once, and the Arrival announced once
            rumInStock() shouldBe rumBefore + 1
            fakes.outbox.shipArrived().count { it.shippingId == returnShippingId } shouldBe 1
        }

        /** Hands the listener Port Royal's Ship Arrived for "Black Pearl" and returns its event id. */
        fun blackPearlArrivedAtPortRoyal(eventId: UUID = UUID.randomUUID()): UUID {
            shippingEventListener.onShippingEvent(
                aShipArrivedRecord(
                    shipId = blackPearlId,
                    shipName = "Black Pearl",
                    shippingId = blackPearlShippingId,
                    originHarbor = "Tortuga",
                    destinationHarbor = "Port Royal",
                    eventId = eventId,
                )
            )
            return eventId
        }

        fun shippingStateOfBlackPearlsVoyage(): Any? {
            val response = restTemplate.getForEntity(
                "/web/ships/$blackPearlId/shippings/$blackPearlShippingId", Map::class.java
            )
            response.statusCode shouldBe HttpStatus.OK
            return response.body!!["shippingState"]
        }
    }

    @Nested
    @TestPropertySource(properties = ["harbor.name=Port Royal"])
    inner class AtPortRoyal : AHarbor() {

        private val blackPearlId: UUID = UUID.randomUUID()
        private val blackPearlShippingId: UUID = UUID.randomUUID()

        /** Background: "Black Pearl" was Released at "Tortuga" to "Port Royal", loaded with "Rum". */
        @BeforeEach
        fun blackPearlIsReleasedAtTortugaToPortRoyal() {
            blackPearlReleasedAtTortuga()
        }

        /** Hands the listener Tortuga's Shipping Published for "Black Pearl" under [eventId] and returns it. */
        fun blackPearlReleasedAtTortuga(eventId: UUID = UUID.randomUUID()): UUID {
            shippingEventListener.onShippingEvent(
                aShippingPublishedRecord(
                    shipId = blackPearlId,
                    shipName = "Black Pearl",
                    catainId = SeedData.aCatainId,
                    shippingId = blackPearlShippingId,
                    cargoIds = listOf(SeedData.cargoIdOf("Rum")),
                    originHarbor = "Tortuga",
                    destinationHarbor = "Port Royal",
                    eventId = eventId,
                )
            )
            return eventId
        }

        @Test
        fun `A re-published Release of a ship that has sailed on is ignored`() {
            // Given "Black Pearl" sailed on from "Port Royal" to "Tortuga", and "Port Royal" learned of its Arrival
            fakes.givenKnownHarbors("Tortuga")
            restTemplate.postForEntity("/web/ships/$blackPearlId/shippings", null, Map::class.java)
                .statusCode shouldBe HttpStatus.OK
            val onwardVoyage = restTemplate.release(blackPearlId, "Tortuga")
            onwardVoyage.statusCode shouldBe HttpStatus.OK
            shippingEventListener.onShippingEvent(
                aShipArrivedRecord(
                    shipId = blackPearlId,
                    shippingId = UUID.fromString(onwardVoyage.body!!["id"] as String),
                    originHarbor = "Port Royal",
                    destinationHarbor = "Tortuga",
                )
            )
            availableShips().none { it["id"] == blackPearlId.toString() } shouldBe true
            val rumBefore = rumInStock()

            // When Tortuga's Release of "Black Pearl" to "Port Royal" is published again under a new event id
            blackPearlReleasedAtTortuga()

            // Then "Black Pearl" does not arrive again: not in the fleet, no Cargo unloaded, no second Ship Arrived
            availableShips().none { it["id"] == blackPearlId.toString() } shouldBe true
            rumInStock() shouldBe rumBefore
            fakes.outbox.shipArrived().count { it.shippingId == blackPearlShippingId } shouldBe 1
        }

        @Test
        fun `An arrived ship can sail back`() {
            // Given "Black Pearl" has arrived at "Port Royal"
            fakes.givenKnownHarbors("Tortuga")

            // When the User at "Port Royal" Releases "Black Pearl" on a new Shipping to "Tortuga"
            restTemplate.postForEntity("/web/ships/$blackPearlId/shippings", null, Map::class.java)
                .statusCode shouldBe HttpStatus.OK
            val release = restTemplate.release(blackPearlId, "Tortuga")

            // Then "Black Pearl" is at sea with Destination Harbor "Tortuga"
            release.statusCode shouldBe HttpStatus.OK
            release.body!!["destinationHarbor"] shouldBe "Tortuga"
            availableShips().single { it["id"] == blackPearlId.toString() }["shippingState"] shouldBe "SHIPPING"
        }
    }

    /** One Harbor on in-memory fakes, reset before every scenario. */
    @FakeHarborTest
    abstract class AHarbor {

        @Autowired
        lateinit var restTemplate: TestRestTemplate

        @Autowired
        lateinit var fakes: FakeDrivenPorts

        @Autowired
        lateinit var shippingEventListener: ShippingEventListener

        @BeforeEach
        fun resetHarbor() {
            fakes.reset()
        }

        /** How much "Rum" this Harbor has in its Stock. */
        fun rumInStock(): Int = fakes.stock.getStock()[CargoId(SeedData.cargoIdOf("Rum"))]!!

        fun availableShips(): List<Map<*, *>> {
            val response = restTemplate.getForEntity("/web/ships", List::class.java)
            response.statusCode shouldBe HttpStatus.OK
            return response.body!!.map { it as Map<*, *> }
        }
    }
}
