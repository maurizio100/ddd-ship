package com.sonicdevelopment.application.acceptance

import com.fasterxml.jackson.databind.ObjectMapper
import com.sonicdevelopment.application.acceptance.fixtures.FakeDrivenPorts
import com.sonicdevelopment.application.acceptance.fixtures.FakeHarborTest
import com.sonicdevelopment.application.acceptance.fixtures.SeedData
import com.sonicdevelopment.application.acceptance.fixtures.aShipArrivedRecord
import com.sonicdevelopment.application.acceptance.fixtures.aShipBeingPrepared
import com.sonicdevelopment.application.acceptance.fixtures.aShippingPublishedRecord
import com.sonicdevelopment.application.acceptance.fixtures.givenKnownHarbors
import com.sonicdevelopment.application.acceptance.fixtures.givenPriceOf
import com.sonicdevelopment.application.acceptance.fixtures.givenSavings
import com.sonicdevelopment.application.acceptance.fixtures.release
import com.sonicdevelopment.driving.adapter.messaging.ShippingEventListener
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestPropertySource
import java.util.*

/**
 * Feature: Refuse an Incoming Ship at another Harbor.
 *
 * Each Harbor is a nested class on in-memory fakes. Another Harbor's events are delivered by handing the
 * [ShippingEventListener] the record Debezium would relay; the delivery is synchronous. The refusal is
 * `POST /web/incoming-ships/{id}/refusal`; state is read over HTTP and from the fake outbox. The refused ship
 * stays in the refusing Harbor's fleet, at sea, until the Home Harbor's `ship-arrived` comes back.
 */
class RefuseAnIncomingShipAcceptanceTest {

    @Nested
    @TestPropertySource(properties = ["harbor.name=Tortuga"])
    inner class AtTortuga : AHarbor() {

        private val rum = SeedData.cargoIdOf("Rum")

        /** Background: "Salty Whisker", Home Harbor "Port Royal", has arrived at "Tortuga" with 2 Rum aboard. */
        @BeforeEach
        fun saltyWhiskerHasArrived() {
            fakes.givenPriceOf("Rum", "40.00")
            arrive(saltyWhiskerId, cargoIds = listOf(rum, rum))
        }

        @Test
        fun `A refused ship sails back to its Home Harbor with its Cargo`() {
            // Given the Stock and the Savings of "Tortuga"
            val stockBefore = getList("/web/stock")
            val savingsBefore = savings()

            // When the User refuses "Salty Whisker"
            refuse(saltyWhiskerId).statusCode shouldBe HttpStatus.NO_CONTENT

            // Then "Salty Whisker" sails to "Port Royal" with its 2 Rum aboard
            val published = fakes.outbox.shippingPublished().single()
            published.shipId shouldBe saltyWhiskerId
            published.destinationHarbor shouldBe "Port Royal"
            published.originHarbor shouldBe "Tortuga"
            published.homeHarbor shouldBe "Port Royal"
            published.cargoIds shouldBe listOf(rum, rum)
            incomingShips().none { it["shipId"] == saltyWhiskerId.toString() } shouldBe true
            ships().single { it["id"] == saltyWhiskerId.toString() }["shippingState"] shouldBe "SHIPPING"

            // And "Salty Whisker" is no longer in the fleet of "Tortuga" once Port Royal has announced its Arrival
            shippingEventListener.onShippingEvent(
                aShipArrivedRecord(
                    shipId = saltyWhiskerId,
                    shipName = "Salty Whisker",
                    shippingId = published.shippingId,
                    originHarbor = "Tortuga",
                    destinationHarbor = "Port Royal",
                )
            )
            ships().none { it["id"] == saltyWhiskerId.toString() } shouldBe true

            // And the Stock and the Savings of "Tortuga" are unchanged
            getList("/web/stock") shouldBe stockBefore
            savings() shouldBe savingsBefore
        }

        @ParameterizedTest(name = "the Savings {0} the Delivery Price")
        @CsvSource("cover,1000.00", "do not cover,10.00")
        fun `A Harbor may refuse whether or not it could pay`(cover: String, savings: String) {
            // Given the Savings <cover> the Delivery Price of 80.00 $
            fakes.givenSavings(savings)

            // When the User refuses "Salty Whisker"
            refuse(saltyWhiskerId).statusCode shouldBe HttpStatus.NO_CONTENT

            // Then "Salty Whisker" sails to "Port Royal"
            fakes.outbox.shippingPublished().single().destinationHarbor shouldBe "Port Royal"
        }

        @Test
        fun `A ship that is not Incoming cannot be refused`() {
            // Given a ship that arrived with nothing aboard
            val emptyShipId = UUID.randomUUID()
            arrive(emptyShipId, cargoIds = emptyList())

            // When the User refuses it
            val refusal = refuse(emptyShipId)

            // Then it is a conflict and nothing is published
            refusal.statusCode shouldBe HttpStatus.CONFLICT
            refusal.body!!["title"] shouldBe "Not an Incoming Ship"
            fakes.outbox.shippingPublished() shouldBe emptyList()
        }

        @Test
        fun `A Home Harbor cannot yet refuse its own ship`() {
            // Given a ship of "Tortuga" itself returned with 2 Rum aboard
            val ownShipId = UUID.randomUUID()
            arrive(ownShipId, cargoIds = listOf(rum, rum), originHarbor = "Port Royal", homeHarbor = "Tortuga")

            // When the User refuses it
            val refusal = refuse(ownShipId)

            // Then it is a conflict and the ship is still Incoming
            refusal.statusCode shouldBe HttpStatus.CONFLICT
            refusal.body!!["title"] shouldBe "Ship at its Home Harbor"
            incomingShips().any { it["shipId"] == ownShipId.toString() } shouldBe true
            fakes.outbox.shippingPublished() shouldBe emptyList()
        }

        private fun arrive(
            shipId: UUID,
            cargoIds: List<UUID>,
            originHarbor: String = "Port Royal",
            homeHarbor: String = "Port Royal",
        ) {
            shippingEventListener.onShippingEvent(
                aShippingPublishedRecord(
                    shipId = shipId,
                    shipName = "Salty Whisker",
                    catainId = SeedData.aCatainId,
                    shippingId = UUID.randomUUID(),
                    cargoIds = cargoIds,
                    originHarbor = originHarbor,
                    destinationHarbor = "Tortuga",
                    homeHarbor = homeHarbor,
                )
            )
        }

        private fun refuse(shipId: UUID) =
            restTemplate.postForEntity("/web/incoming-ships/$shipId/refusal", null, Map::class.java)

        private fun savings(): String {
            val response = restTemplate.getForEntity("/web/savings", String::class.java)
            response.statusCode shouldBe HttpStatus.OK
            return ObjectMapper().readValue(response.body!!, Map::class.java)["amount"] as String
        }
    }

    @Nested
    @TestPropertySource(properties = ["harbor.name=Port Royal"])
    inner class AtPortRoyal : AHarbor() {

        private val rum = SeedData.cargoIdOf("Rum")
        lateinit var releasedShippingId: UUID
        lateinit var refusalShippingId: UUID

        /** Background: "Salty Whisker" was registered at "Port Royal" and Released to "Tortuga". */
        @BeforeEach
        fun saltyWhiskerIsReleasedToTortuga() {
            fakes.givenKnownHarbors("Tortuga")
            val shipId = restTemplate.aShipBeingPrepared("Salty Whisker")
            val release = restTemplate.release(shipId, "Tortuga")
            release.statusCode shouldBe HttpStatus.OK
            releasedShippingId = UUID.fromString(release.body!!["id"] as String)
            saltyWhiskerIdAtPortRoyal = shipId
            refusalShippingId = UUID.randomUUID()
        }

        lateinit var saltyWhiskerIdAtPortRoyal: UUID

        @Test
        fun `The refused ship arrives at its Home Harbor as an Incoming Ship`() {
            // Given "Tortuga" refused "Salty Whisker" and its Ship Arrived has been consumed
            tortugaAnnouncesTheArrival()

            // When "Salty Whisker" arrives at "Port Royal"
            tortugaRefusalArrives()

            // Then "Salty Whisker" is an Incoming Ship at "Port Royal" with its 2 Rum aboard
            val incoming = incomingShips().single { it["shipId"] == saltyWhiskerIdAtPortRoyal.toString() }
            (incoming["cargo"] as List<*>).map { (it as Map<*, *>)["name"] } shouldBe listOf("Rum", "Rum")
            ships().single { it["id"] == saltyWhiskerIdAtPortRoyal.toString() }["homeHarbor"] shouldBe "Port Royal"
        }

        @Test
        fun `A refused ship that reaches its Home Harbor before that Harbor heard of its Arrival elsewhere is still an Incoming Ship`() {
            // When the refusal arrives first and only then Tortuga's Ship Arrived
            tortugaRefusalArrives()
            tortugaAnnouncesTheArrival()

            // Then the earlier voyage ends DONE
            val voyage = restTemplate.getForEntity(
                "/web/ships/$saltyWhiskerIdAtPortRoyal/shippings/$releasedShippingId", Map::class.java
            )
            voyage.statusCode shouldBe HttpStatus.OK
            voyage.body!!["shippingState"] shouldBe "DONE"
            // And the ship is an Incoming Ship with 2 Rum
            val incoming = incomingShips().single { it["shipId"] == saltyWhiskerIdAtPortRoyal.toString() }
            (incoming["cargo"] as List<*>).map { (it as Map<*, *>)["name"] } shouldBe listOf("Rum", "Rum")
        }

        private fun tortugaAnnouncesTheArrival() {
            shippingEventListener.onShippingEvent(
                aShipArrivedRecord(
                    shipId = saltyWhiskerIdAtPortRoyal,
                    shipName = "Salty Whisker",
                    shippingId = releasedShippingId,
                    originHarbor = "Port Royal",
                    destinationHarbor = "Tortuga",
                )
            )
        }

        /** The Shipping Published that Tortuga's refusal writes, delivered to "Port Royal". */
        private fun tortugaRefusalArrives() {
            shippingEventListener.onShippingEvent(
                aShippingPublishedRecord(
                    shipId = saltyWhiskerIdAtPortRoyal,
                    shipName = "Salty Whisker",
                    catainId = SeedData.aCatainId,
                    shippingId = refusalShippingId,
                    cargoIds = listOf(rum, rum),
                    originHarbor = "Tortuga",
                    destinationHarbor = "Port Royal",
                    homeHarbor = "Port Royal",
                )
            )
        }
    }

    /** One Harbor on in-memory fakes. */
    @FakeHarborTest
    abstract class AHarbor {

        @Autowired
        lateinit var restTemplate: TestRestTemplate

        @Autowired
        lateinit var fakes: FakeDrivenPorts

        @Autowired
        lateinit var shippingEventListener: ShippingEventListener

        val saltyWhiskerId: UUID = UUID.randomUUID()

        @BeforeEach
        fun aFreshHarbor() {
            fakes.reset()
        }

        fun ships(): List<Map<*, *>> = getList("/web/ships")

        fun incomingShips(): List<Map<*, *>> = getList("/web/incoming-ships")

        fun getList(url: String): List<Map<*, *>> {
            val response = restTemplate.getForEntity(url, String::class.java)
            response.statusCode shouldBe HttpStatus.OK
            return ObjectMapper().readValue(response.body!!, List::class.java).map { it as Map<*, *> }
        }
    }
}
