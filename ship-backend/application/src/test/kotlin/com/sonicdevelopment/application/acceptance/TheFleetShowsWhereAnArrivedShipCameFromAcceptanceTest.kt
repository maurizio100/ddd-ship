package com.sonicdevelopment.application.acceptance

import com.sonicdevelopment.application.acceptance.fixtures.FakeDrivenPorts
import com.sonicdevelopment.application.acceptance.fixtures.FakeHarborTest
import com.sonicdevelopment.application.acceptance.fixtures.SeedData
import com.sonicdevelopment.application.acceptance.fixtures.aShipArrivedRecord
import com.sonicdevelopment.application.acceptance.fixtures.aShippingPublishedRecord
import com.sonicdevelopment.application.acceptance.fixtures.givenKnownHarbors
import com.sonicdevelopment.application.acceptance.fixtures.release
import com.sonicdevelopment.driving.adapter.messaging.ShippingEventListener
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestPropertySource
import java.util.*

/**
 * Feature: The fleet shows where an arrived ship came from.
 *
 * The scenarios run at "Port Royal", on in-memory fakes. An Arrival is simulated by handing the listener the
 * `shipping-published` record Debezium would relay from the Origin Harbor's outbox; the delivery is synchronous.
 */
@FakeHarborTest
@TestPropertySource(properties = ["harbor.name=Port Royal"])
class TheFleetShowsWhereAnArrivedShipCameFromAcceptanceTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var fakes: FakeDrivenPorts

    @Autowired
    lateinit var shippingEventListener: ShippingEventListener

    private val blackPearlId: UUID = UUID.randomUUID()

    @BeforeEach
    fun resetHarbor() {
        fakes.reset()
    }

    @Test
    fun `An arrived ship shows its Origin Harbor`() {
        // Given "Black Pearl" arrived at "Port Royal" from "Tortuga"
        blackPearlArrivesFrom("Tortuga")

        // Then "Port Royal" lists "Black Pearl" as arrived from "Tortuga"
        fleetEntryOf(blackPearlId)["arrivedFrom"] shouldBe "Tortuga"
        detailsOf(blackPearlId)["arrivedFrom"] shouldBe "Tortuga"
    }

    @Test
    fun `A ship registered at the Harbor shows no Origin Harbor`() {
        // Given "Interceptor" was registered at "Port Royal"
        val registered = restTemplate.postForEntity(
            "/web/ships", mapOf("name" to "Interceptor", "catainId" to SeedData.aCatainId), Map::class.java
        )
        registered.statusCode shouldBe HttpStatus.OK
        val interceptorId = UUID.fromString(registered.body!!["id"] as String)

        // Then "Port Royal" lists "Interceptor" without an Origin Harbor
        val listed = fleetEntryOf(interceptorId)
        listed["name"] shouldBe "Interceptor"
        listed.containsKey("arrivedFrom") shouldBe true
        listed["arrivedFrom"] shouldBe null
        val details = detailsOf(interceptorId)
        details.containsKey("arrivedFrom") shouldBe true
        details["arrivedFrom"] shouldBe null
    }

    @Test
    fun `A ship shows the Harbor of its latest Arrival`() {
        // Given "Black Pearl" arrived at "Port Royal" from "Tortuga"
        blackPearlArrivesFrom("Tortuga")
        // ... sailed on to "Nassau", and "Port Royal" learned of its Arrival there
        fakes.givenKnownHarbors("Nassau")
        restTemplate.postForEntity("/web/ships/$blackPearlId/shippings", null, Map::class.java)
            .statusCode shouldBe HttpStatus.OK
        val toNassau = restTemplate.release(blackPearlId, "Nassau")
        toNassau.statusCode shouldBe HttpStatus.OK
        shippingEventListener.onShippingEvent(
            aShipArrivedRecord(
                shipId = blackPearlId,
                shippingId = UUID.fromString(toNassau.body!!["id"] as String),
                originHarbor = "Port Royal",
                destinationHarbor = "Nassau",
            )
        )
        availableShips().none { it["id"] == blackPearlId.toString() } shouldBe true

        // And "Black Pearl" later arrived at "Port Royal" again from "Nassau"
        blackPearlArrivesFrom("Nassau")

        // Then "Port Royal" lists "Black Pearl" as arrived from "Nassau"
        fleetEntryOf(blackPearlId)["arrivedFrom"] shouldBe "Nassau"
        detailsOf(blackPearlId)["arrivedFrom"] shouldBe "Nassau"
    }

    /** Hands the listener the Origin Harbor's Shipping Published of "Black Pearl" to "Port Royal". */
    private fun blackPearlArrivesFrom(originHarbor: String) {
        shippingEventListener.onShippingEvent(
            aShippingPublishedRecord(
                shipId = blackPearlId,
                shipName = "Black Pearl",
                catainId = SeedData.aCatainId,
                shippingId = UUID.randomUUID(),
                originHarbor = originHarbor,
                destinationHarbor = "Port Royal",
            )
        )
    }

    private fun fleetEntryOf(shipId: UUID): Map<*, *> =
        availableShips().single { it["id"] == shipId.toString() }

    private fun detailsOf(shipId: UUID): Map<*, *> {
        val response = restTemplate.getForEntity("/web/ships/$shipId", Map::class.java)
        response.statusCode shouldBe HttpStatus.OK
        return response.body!!
    }

    private fun availableShips(): List<Map<*, *>> {
        val response = restTemplate.getForEntity("/web/ships", List::class.java)
        response.statusCode shouldBe HttpStatus.OK
        return response.body!!.map { it as Map<*, *> }
    }
}
