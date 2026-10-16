package com.sonicdevelopment.application.acceptance

import com.sonicdevelopment.application.acceptance.fixtures.FakeDrivenPorts
import com.sonicdevelopment.application.acceptance.fixtures.FakeHarborTest
import com.sonicdevelopment.application.acceptance.fixtures.SeedData
import com.sonicdevelopment.application.acceptance.fixtures.aShipBeingPrepared
import com.sonicdevelopment.application.acceptance.fixtures.aShippingPublishedRecord
import com.sonicdevelopment.driving.adapter.messaging.ShippingEventListener
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
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
 * Feature: A ship carries more than one of the same Cargo.
 *
 * Background: a ship at "Tortuga" with a new Shipping, and the Stock holding the Starting Stock
 * (3) of every Cargo, "Rum" (weight 5.5) and "Sugar" (weight 0.7) included.
 */
class AShipCarriesMoreThanOneOfTheSameCargoAcceptanceTest {

    @Nested
    inner class LoadingAndUnloading : AHarbor() {

        @Test
        fun `loading the same Cargo again adds another one aboard`() {
            // Given 1 Rum is among the Loaded Cargo
            val shipId = restTemplate.aShipBeingPrepared()
            load(shipId, "Rum").statusCode shouldBe HttpStatus.OK

            // When the User loads Rum
            val load = load(shipId, "Rum")

            // Then 2 Rum are among the Loaded Cargo
            load.statusCode shouldBe HttpStatus.OK
            loadedCargoCountOf(shipId, "Rum") shouldBe 2
            // And the Current Weight is 11.00
            currentWeight(shipId) shouldBe 11.0
            // And the Stock holds 1 Rum
            stockOf("Rum") shouldBe 1
        }

        @Test
        fun `the Max Weight applies to everything aboard`() {
            // Given 2 Rum are among the Loaded Cargo
            val shipId = restTemplate.aShipBeingPrepared()
            load(shipId, "Rum").statusCode shouldBe HttpStatus.OK
            load(shipId, "Rum").statusCode shouldBe HttpStatus.OK

            // When the User loads Rum
            val load = load(shipId, "Rum")

            // Then the User is told that Rum would exceed the Max Weight of 15.0
            load.statusCode shouldBe HttpStatus.CONFLICT
            (load.body!!["detail"] as String) shouldContain "Rum would exceed the Max Weight of 15.0"
            // And 2 Rum are among the Loaded Cargo
            loadedCargoCountOf(shipId, "Rum") shouldBe 2
        }

        @Test
        fun `a Cargo cannot be loaded more often than the Stock holds`() {
            // Given 3 Sugar are among the Loaded Cargo
            val shipId = restTemplate.aShipBeingPrepared()
            repeat(3) { load(shipId, "Sugar").statusCode shouldBe HttpStatus.OK }

            // When the User loads Sugar
            val load = load(shipId, "Sugar")

            // Then the User is told that Sugar is out of Stock
            load.statusCode shouldBe HttpStatus.CONFLICT
            load.body!!["detail"] shouldBe "Sugar is out of Stock"
            // And 3 Sugar are among the Loaded Cargo
            loadedCargoCountOf(shipId, "Sugar") shouldBe 3
        }

        @Test
        fun `unloading takes one off the ship and puts it back into the Stock`() {
            // Given 2 Rum are among the Loaded Cargo
            val shipId = restTemplate.aShipBeingPrepared()
            load(shipId, "Rum").statusCode shouldBe HttpStatus.OK
            load(shipId, "Rum").statusCode shouldBe HttpStatus.OK

            // When the User unloads Rum
            val unload = unload(shipId, "Rum")

            // Then 1 Rum is among the Loaded Cargo
            unload.statusCode shouldBe HttpStatus.OK
            loadedCargoCountOf(shipId, "Rum") shouldBe 1
            // And the Stock holds 2 Rum
            stockOf("Rum") shouldBe 2
        }
    }

    @Nested
    inner class ArrivalAtTheDestinationHarbor : AHarbor() {

        @Test
        fun `the Destination Harbor receives every Cargo aboard`() {
            // Given 2 Rum and 1 Sugar are among the Loaded Cargo and the ship is Released to "Tortuga":
            // simulated by the shipping-published record Debezium would relay, carrying two Rum and one Sugar
            val rumId = SeedData.cargoIdOf("Rum")
            val sugarId = SeedData.cargoIdOf("Sugar")
            val eventId = UUID.randomUUID()
            val shipId = UUID.randomUUID()

            // When the ship arrives at "Tortuga"
            shippingEventListener.onShippingEvent(
                aShippingPublishedRecord(
                    shipId = shipId,
                    catainId = SeedData.aCatainId,
                    cargoIds = listOf(rumId, rumId, sugarId),
                    originHarbor = "Nassau",
                    destinationHarbor = "Tortuga",
                    eventId = eventId,
                )
            )

            // Then the ship is an Incoming Ship at "Tortuga" with its 2 Rum and 1 Sugar still aboard (STORY-044)
            val incoming = incomingShips().single { it["shipId"] == shipId.toString() }
            (incoming["cargo"] as List<*>).map { (it as Map<*, *>)["name"] as String }.sorted() shouldBe
                listOf("Rum", "Rum", "Sugar")
            // And the Stock of "Tortuga" is unchanged: the Cargo stays aboard until it is unloaded
            stockOf("Rum") shouldBe 3
            stockOf("Sugar") shouldBe 3
        }
    }

    /** The Harbor "Tortuga" on in-memory fakes; inbound events are handed to the listener, which is synchronous. */
    @FakeHarborTest
    @TestPropertySource(properties = ["harbor.name=Tortuga"])
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

        fun load(shipId: UUID, cargoName: String) =
            restTemplate.postForEntity(
                "/web/ships/$shipId/cargos", mapOf("cargoId" to SeedData.cargoIdOf(cargoName)), Map::class.java
            )

        fun unload(shipId: UUID, cargoName: String) =
            restTemplate.exchange(
                "/web/ships/$shipId/cargos/${SeedData.cargoIdOf(cargoName)}", HttpMethod.DELETE, null, Map::class.java
            )

        fun availableCargo(): List<Map<*, *>> {
            val response = restTemplate.getForEntity("/web/cargos", List::class.java)
            response.statusCode shouldBe HttpStatus.OK
            return response.body!!.map { it as Map<*, *> }
        }

        /** The Stock of [cargoName] as the User sees it among the Available Cargo; 0 when it isn't offered. */
        fun stockOf(cargoName: String): Int =
            (availableCargo().singleOrNull { it["name"] == cargoName }?.get("stock") as Int?) ?: 0

        fun incomingShips(): List<Map<*, *>> {
            val response = restTemplate.getForEntity("/web/incoming-ships", List::class.java)
            response.statusCode shouldBe HttpStatus.OK
            return response.body!!.map { it as Map<*, *> }
        }

        fun ship(shipId: UUID): Map<*, *> {
            val response = restTemplate.getForEntity("/web/ships/$shipId", Map::class.java)
            response.statusCode shouldBe HttpStatus.OK
            return response.body!!
        }

        fun loadedCargoNames(shipId: UUID): List<String> =
            (ship(shipId)["cargo"] as List<*>).map { (it as Map<*, *>)["name"] as String }

        fun loadedCargoCountOf(shipId: UUID, cargoName: String): Int =
            loadedCargoNames(shipId).count { it == cargoName }

        fun currentWeight(shipId: UUID): Double = (ship(shipId)["weight"] as Number).toDouble()
    }
}
