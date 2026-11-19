package com.sonicdevelopment.application.acceptance

import com.fasterxml.jackson.databind.ObjectMapper
import com.sonicdevelopment.application.acceptance.fixtures.FakeDrivenPorts
import com.sonicdevelopment.application.acceptance.fixtures.FakeHarborTest
import com.sonicdevelopment.application.acceptance.fixtures.FleetEvent
import com.sonicdevelopment.application.acceptance.fixtures.FleetEventStream
import com.sonicdevelopment.application.acceptance.fixtures.SeedData
import com.sonicdevelopment.application.acceptance.fixtures.aShipImprisonedRecord
import com.sonicdevelopment.application.acceptance.fixtures.aShippingPublishedRecord
import com.sonicdevelopment.application.acceptance.fixtures.givenKnownHarbors
import com.sonicdevelopment.application.acceptance.fixtures.release
import com.sonicdevelopment.driving.adapter.messaging.CoastGuardEventListener
import com.sonicdevelopment.driving.adapter.messaging.ShippingEventListener
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
 * Feature: A Harbor carries out an imprisonment.
 *
 * Each Harbor is a nested class on in-memory fakes with a User looking at its fleet. The Coast Guard's
 * `ship-imprisoned` and other Harbors' Shipping Published are delivered by handing the listeners the record
 * Debezium would relay; the delivery is synchronous. State is read over HTTP, from the fleet-events stream and
 * from the fake outbox. Background: "Salty Whisker" has the Home Harbor "Tortuga".
 */
class AHarborCarriesOutAnImprisonmentAcceptanceTest {

    @Nested
    @TestPropertySource(properties = ["harbor.name=Port Royal"])
    inner class AtPortRoyal : AHarbor() {

        @Test
        fun `Away from its Home Harbor the ship's Cargo is confiscated and it is sent home`() {
            // Given "Salty Whisker" has arrived here with 2 Rum and 1 Silk aboard
            arrive(listOf(rum, rum, silk))
            val stockBefore = getList("/web/stock")

            // When the Coast Guard imprisons "Salty Whisker"
            coastGuardImprisonsSaltyWhisker()

            // Then its Cargo is confiscated and the Stock is unchanged
            cargoAboard() shouldBe emptyList()
            getList("/web/stock") shouldBe stockBefore
            // And "Salty Whisker" sails to "Tortuga" as an imprisoned ship, without a User Releasing it
            val published = fakes.outbox.shippingPublished().single()
            published.shipId shouldBe saltyWhiskerId
            published.originHarbor shouldBe "Port Royal"
            published.destinationHarbor shouldBe "Tortuga"
            published.homeHarbor shouldBe "Tortuga"
            published.cargoIds shouldBe emptyList()
            published.imprisoned shouldBe true
            ship()["shippingState"] shouldBe "SHIPPING"
        }

        @Test
        fun `The User is told when the Coast Guard imprisons a ship here`() {
            // Given "Salty Whisker" has arrived here with 2 Rum aboard
            arrive(listOf(rum, rum))

            // When the Coast Guard imprisons "Salty Whisker"
            coastGuardImprisonsSaltyWhisker()

            // Then the User is told that the Coast Guard imprisoned "Salty Whisker", its Cargo is confiscated and it sails home to "Tortuga"
            val pushed = fleet.nextAbout("ship-imprisoned", saltyWhiskerId)
            pushed.data["shipName"] shouldBe "Salty Whisker"
            pushed.data["homeHarbor"] shouldBe "Tortuga"
            pushed.data["sailsHome"] shouldBe true
        }

        @Test
        fun `A ship that has already sailed on escapes`() {
            // Given "Salty Whisker" arrived here and has since been Released to "Isla de Muerta" carrying 1 Rum
            fakes.givenKnownHarbors("Isla de Muerta")
            arrive(emptyList())
            restTemplate.postForEntity("/web/ships/$saltyWhiskerId/shippings", null, Map::class.java)
                .statusCode shouldBe HttpStatus.OK
            restTemplate.postForEntity("/web/ships/$saltyWhiskerId/cargos", mapOf("cargoId" to rum), Map::class.java)
                .statusCode shouldBe HttpStatus.OK
            restTemplate.release(saltyWhiskerId, "Isla de Muerta").statusCode shouldBe HttpStatus.OK

            // When the Coast Guard imprisons "Salty Whisker"
            coastGuardImprisonsSaltyWhisker()

            // Then "Salty Whisker" is still at sea to "Isla de Muerta" with its Cargo
            val published = fakes.outbox.shippingPublished().single()
            published.destinationHarbor shouldBe "Isla de Muerta"
            published.cargoIds shouldBe listOf(rum)
            published.imprisoned shouldBe false
            ship()["shippingState"] shouldBe "SHIPPING"
            ship()["imprisoned"] shouldBe false
        }

        @Test
        fun `An imprisonment is carried out once`() {
            // Given "Salty Whisker" has arrived here with 2 Rum aboard
            arrive(listOf(rum, rum))

            // When the Coast Guard's imprisonment of "Salty Whisker" arrives twice
            val record = aShipImprisonedRecord(
                shipId = saltyWhiskerId, shipName = "Salty Whisker", holdingHarbor = "Port Royal", homeHarbor = "Tortuga",
            )
            coastGuardEventListener.onCoastGuardEvent(record)
            coastGuardEventListener.onCoastGuardEvent(record)

            // Then "Salty Whisker" is sent home to "Tortuga" only once
            val published = fakes.outbox.shippingPublished().single()
            published.destinationHarbor shouldBe "Tortuga"
            published.imprisoned shouldBe true
        }

        private fun arrive(cargoIds: List<UUID>) = arriveFromTortuga(cargoIds, originHarbor = "Tortuga")
    }

    @Nested
    @TestPropertySource(properties = ["harbor.name=Tortuga"])
    inner class AtTortuga : AHarbor() {

        @Test
        fun `At its Home Harbor the ship is imprisoned in place`() {
            // Given "Salty Whisker" has arrived here with 2 Rum aboard
            arriveFromTortuga(listOf(rum, rum), originHarbor = "Port Royal")
            val stockBefore = getList("/web/stock")

            // When the Coast Guard imprisons "Salty Whisker"
            coastGuardEventListener.onCoastGuardEvent(
                aShipImprisonedRecord(
                    shipId = saltyWhiskerId, shipName = "Salty Whisker", holdingHarbor = "Tortuga", homeHarbor = "Tortuga",
                )
            )

            // Then its Cargo is confiscated and the Stock is unchanged
            cargoAboard() shouldBe emptyList()
            getList("/web/stock") shouldBe stockBefore
            fakes.outbox.shippingPublished() shouldBe emptyList()
            // And the fleet shows "Salty Whisker" as an Imprisoned Ship
            ship()["imprisoned"] shouldBe true
            getList("/web/incoming-ships").none { it["shipId"] == saltyWhiskerId.toString() } shouldBe true
        }

        @Test
        fun `A ship sent home imprisoned is imprisoned on its Arrival`() {
            // Given "Port Royal" sent "Salty Whisker" home as an imprisoned ship
            // When "Salty Whisker" arrives
            arriveFromTortuga(emptyList(), originHarbor = "Port Royal", imprisoned = true)

            // Then the User is told that "Salty Whisker" arrived from "Port Royal", imprisoned
            val pushed = fleet.nextAbout("ship-arrived", saltyWhiskerId)
            pushed.data["shipName"] shouldBe "Salty Whisker"
            pushed.data["originHarbor"] shouldBe "Port Royal"
            pushed.data["imprisoned"] shouldBe true
            // And "Salty Whisker" is an Imprisoned Ship with no Cargo aboard
            ship()["imprisoned"] shouldBe true
            getList("/web/incoming-ships").none { it["shipId"] == saltyWhiskerId.toString() } shouldBe true
            cargoAboard() shouldBe emptyList()
        }

        @Test
        fun `An Imprisoned Ship cannot get a new Shipping`() {
            // Given "Salty Whisker" is an Imprisoned Ship
            arriveFromTortuga(emptyList(), originHarbor = "Port Royal", imprisoned = true)
            val shippingStateBefore = ship()["shippingState"]

            // When the User starts a new Shipping for "Salty Whisker"
            val response = restTemplate.postForEntity("/web/ships/$saltyWhiskerId/shippings", null, Map::class.java)

            // Then the User is told that "Salty Whisker" is imprisoned
            response.statusCode shouldBe HttpStatus.CONFLICT
            response.body!!["detail"] shouldBe "Salty Whisker is imprisoned"
            // And "Salty Whisker" has no new Shipping
            ship()["shippingState"] shouldBe shippingStateBefore
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

        @Autowired
        lateinit var coastGuardEventListener: CoastGuardEventListener

        @LocalServerPort
        var port: Int = 0

        lateinit var fleet: FleetEventStream

        val saltyWhiskerId: UUID = UUID.randomUUID()
        val rum: UUID = SeedData.cargoIdOf("Rum")
        val silk: UUID = SeedData.cargoIdOf("Silk")

        @BeforeEach
        fun aUserIsLookingAtTheFleet() {
            fakes.reset()
            fleet = FleetEventStream.open("http://localhost:$port")
        }

        @AfterEach
        fun theUserLooksAway() {
            fleet.close()
        }

        /** "Salty Whisker", Home Harbor "Tortuga", arrives here from [originHarbor] with [cargoIds] aboard. */
        fun arriveFromTortuga(cargoIds: List<UUID>, originHarbor: String, imprisoned: Boolean? = null) {
            shippingEventListener.onShippingEvent(
                aShippingPublishedRecord(
                    shipId = saltyWhiskerId,
                    shipName = "Salty Whisker",
                    catainId = SeedData.aCatainId,
                    cargoIds = cargoIds,
                    originHarbor = originHarbor,
                    destinationHarbor = if (originHarbor == "Tortuga") "Port Royal" else "Tortuga",
                    homeHarbor = "Tortuga",
                    imprisoned = imprisoned,
                )
            )
        }

        fun coastGuardImprisonsSaltyWhisker() {
            coastGuardEventListener.onCoastGuardEvent(
                aShipImprisonedRecord(
                    shipId = saltyWhiskerId, shipName = "Salty Whisker", holdingHarbor = "Port Royal", homeHarbor = "Tortuga",
                )
            )
        }

        fun ship(): Map<*, *> = getList("/web/ships").single { it["id"] == saltyWhiskerId.toString() }

        fun cargoAboard(): List<String> {
            val response = restTemplate.getForEntity("/web/ships/$saltyWhiskerId", Map::class.java)
            response.statusCode shouldBe HttpStatus.OK
            return (response.body!!["cargoAboard"] as List<*>).map { (it as Map<*, *>)["name"] as String }
        }

        fun getList(url: String): List<Map<*, *>> {
            val response = restTemplate.getForEntity(url, String::class.java)
            response.statusCode shouldBe HttpStatus.OK
            return ObjectMapper().readValue(response.body!!, List::class.java).map { it as Map<*, *> }
        }

        /** The first pushed event named [name] about the ship [shipId]; fails if it never comes. */
        fun FleetEventStream.nextAbout(name: String, shipId: UUID): FleetEvent {
            while (true) {
                val event = next().shouldNotBeNull()
                if (event.name == name && event.data["shipId"] == shipId.toString()) return event
            }
        }
    }
}
