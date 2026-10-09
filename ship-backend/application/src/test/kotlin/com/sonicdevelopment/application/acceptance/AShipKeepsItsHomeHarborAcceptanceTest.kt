package com.sonicdevelopment.application.acceptance

import com.fasterxml.jackson.databind.ObjectMapper
import com.sonicdevelopment.application.acceptance.fixtures.FakeDrivenPorts
import com.sonicdevelopment.application.acceptance.fixtures.FakeHarborTest
import com.sonicdevelopment.application.acceptance.fixtures.SeedData
import com.sonicdevelopment.application.acceptance.fixtures.aShippingPublishedRecord
import com.sonicdevelopment.application.acceptance.fixtures.givenKnownHarbors
import com.sonicdevelopment.application.acceptance.fixtures.release
import com.sonicdevelopment.driving.adapter.messaging.ShippingEventListener
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestPropertySource
import java.util.*

/**
 * Feature: A ship keeps its Home Harbor.
 *
 * One nested class per Harbor, each on in-memory fakes. The Release at another Harbor is simulated by handing the
 * listener the `shipping-published` record Debezium would relay; the delivery is synchronous. State is read over
 * HTTP: `GET /web/ships` and `GET /web/ships/{id}`.
 */
class AShipKeepsItsHomeHarborAcceptanceTest {

    @Nested
    @TestPropertySource(properties = ["harbor.name=Port Royal"])
    inner class AtPortRoyal : AHarbor() {

        @Test
        fun `A newly registered ship has this Harbor as its Home Harbor`() {
            // Given the Harbor is named "Port Royal"
            // When the User creates the ship "Salty Whisker"
            val created = restTemplate.postForEntity(
                "/web/ships", mapOf("name" to "Salty Whisker", "catainId" to SeedData.aCatainId), Map::class.java
            )
            created.statusCode shouldBe HttpStatus.OK
            val id = UUID.fromString(created.body!!["id"] as String)

            // Then the Home Harbor of "Salty Whisker" is "Port Royal"
            created.body!!["homeHarbor"] shouldBe "Port Royal"
            fleetEntryOf(id)["homeHarbor"] shouldBe "Port Royal"
            detailsOf(id)["homeHarbor"] shouldBe "Port Royal"
        }

        @Test
        fun `Releasing a ship publishes its Home Harbor`() {
            // Given "Salty Whisker" was registered at "Port Royal"
            fakes.givenKnownHarbors("Tortuga")
            val id = registerSaltyWhisker()

            // When "Salty Whisker" is Released to "Tortuga"
            startShipping(id)
            restTemplate.release(id, "Tortuga").statusCode shouldBe HttpStatus.OK

            // Then the published Shipping carries the Home Harbor "Port Royal"
            fakes.outbox.shippingPublished().single().homeHarbor shouldBe "Port Royal"
        }
    }

    @Nested
    @TestPropertySource(properties = ["harbor.name=Tortuga"])
    inner class AtTortuga : AHarbor() {

        @Test
        fun `The Home Harbor does not change when the ship sails elsewhere`() {
            // Given "Salty Whisker" has the Home Harbor "Port Royal" and is Released to "Tortuga"
            // When "Salty Whisker" arrives at "Tortuga"
            saltyWhiskerArrivesFrom("Port Royal", homeHarbor = "Port Royal")

            // Then the Home Harbor of "Salty Whisker" at "Tortuga" is "Port Royal"
            fleetEntryOf(saltyWhiskerId)["homeHarbor"] shouldBe "Port Royal"
            detailsOf(saltyWhiskerId)["homeHarbor"] shouldBe "Port Royal"
        }

        @Test
        fun `A ship that arrived passes its Home Harbor on when Released again`() {
            // Given "Salty Whisker", whose Home Harbor is "Port Royal", arrived at "Tortuga"
            fakes.givenKnownHarbors("Isla de Muerta")
            saltyWhiskerArrivesFrom("Port Royal", homeHarbor = "Port Royal")

            // When "Salty Whisker" is Released to "Isla de Muerta"
            startShipping(saltyWhiskerId)
            restTemplate.release(saltyWhiskerId, "Isla de Muerta").statusCode shouldBe HttpStatus.OK

            // Then the published Shipping carries the Home Harbor "Port Royal", not "Tortuga"
            fakes.outbox.shippingPublished().single().homeHarbor shouldBe "Port Royal"
        }

        @Test
        fun `A ship from a Harbor that does not send the Home Harbor gets its Origin Harbor`() {
            // When a ship arrives from "Port Royal" in an event without a Home Harbor
            saltyWhiskerArrivesFrom("Port Royal", homeHarbor = null)

            // Then its Home Harbor is "Port Royal"
            fleetEntryOf(saltyWhiskerId)["homeHarbor"] shouldBe "Port Royal"
            detailsOf(saltyWhiskerId)["homeHarbor"] shouldBe "Port Royal"
        }
    }

    @Nested
    @TestPropertySource(properties = ["harbor.name=Isla de Muerta"])
    inner class AtIslaDeMuerta : AHarbor() {

        @Test
        fun `The Home Harbor survives several voyages`() {
            // Given "Salty Whisker" sailed from "Port Royal" to "Tortuga" and on to "Isla de Muerta"
            saltyWhiskerArrivesFrom("Tortuga", homeHarbor = "Port Royal")

            // When the User looks at the Available Ships of "Isla de Muerta"
            // Then "Salty Whisker" is shown with the Home Harbor "Port Royal"
            // (neither this Harbor nor the Origin Harbor)
            fleetEntryOf(saltyWhiskerId)["name"] shouldBe "Salty Whisker"
            fleetEntryOf(saltyWhiskerId)["homeHarbor"] shouldBe "Port Royal"
            detailsOf(saltyWhiskerId)["homeHarbor"] shouldBe "Port Royal"
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

        @Autowired
        lateinit var environment: Environment

        val saltyWhiskerId: UUID = UUID.randomUUID()

        @BeforeEach
        fun aFreshHarbor() {
            fakes.reset()
        }

        /** Hands the listener the Origin Harbor's Shipping Published of "Salty Whisker" to this Harbor. */
        fun saltyWhiskerArrivesFrom(originHarbor: String, homeHarbor: String?) {
            shippingEventListener.onShippingEvent(
                aShippingPublishedRecord(
                    shipId = saltyWhiskerId,
                    shipName = "Salty Whisker",
                    catainId = SeedData.aCatainId,
                    shippingId = UUID.randomUUID(),
                    originHarbor = originHarbor,
                    destinationHarbor = environment.getProperty("harbor.name")!!,
                    homeHarbor = homeHarbor,
                )
            )
        }

        fun registerSaltyWhisker(): UUID {
            val created = restTemplate.postForEntity(
                "/web/ships", mapOf("name" to "Salty Whisker", "catainId" to SeedData.aCatainId), Map::class.java
            )
            created.statusCode shouldBe HttpStatus.OK
            return UUID.fromString(created.body!!["id"] as String)
        }

        fun startShipping(shipId: UUID) {
            restTemplate.postForEntity("/web/ships/$shipId/shippings", null, Map::class.java)
                .statusCode shouldBe HttpStatus.OK
        }

        fun fleetEntryOf(shipId: UUID): Map<*, *> {
            val response = restTemplate.getForEntity("/web/ships", String::class.java)
            response.statusCode shouldBe HttpStatus.OK
            return ObjectMapper().readValue(response.body!!, List::class.java)
                .map { it as Map<*, *> }
                .single { it["id"] == shipId.toString() }
        }

        fun detailsOf(shipId: UUID): Map<*, *> {
            val response = restTemplate.getForEntity("/web/ships/$shipId", Map::class.java)
            response.statusCode shouldBe HttpStatus.OK
            return response.body!!
        }
    }
}
