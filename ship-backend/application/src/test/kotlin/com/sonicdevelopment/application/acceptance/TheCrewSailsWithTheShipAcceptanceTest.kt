package com.sonicdevelopment.application.acceptance

import com.fasterxml.jackson.databind.ObjectMapper
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
import java.util.UUID

/**
 * Feature: The Crew sails with the ship.
 *
 * This Harbor is "Tortuga", on in-memory fakes. "Salty Whisker" has the Home Harbor "Port Royal" and the Crew
 * "Mittens" (Strong Paws) and "Patch" (Sharp Eyes). Its Arrival is simulated by handing the listener the
 * `shipping-published` record Debezium would relay from "Port Royal". State is read over HTTP.
 */
@FakeHarborTest
@TestPropertySource(properties = ["harbor.name=Tortuga"])
class TheCrewSailsWithTheShipAcceptanceTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var fakes: FakeDrivenPorts

    @Autowired
    lateinit var shippingEventListener: ShippingEventListener

    private val saltyWhiskerId = UUID.randomUUID()
    private val mittens = Triple(UUID.randomUUID(), "Mittens", "Strong Paws")
    private val patch = Triple(UUID.randomUUID(), "Patch", "Sharp Eyes")
    private val saffron = Triple(UUID.randomUUID(), "Saffron", "Steady Hands")

    @BeforeEach
    fun aFreshHarbor() {
        fakes.reset()
        fakes.givenKnownHarbors("Port Royal")
    }

    @Test
    fun `The Crew arrives with the ship`() {
        // Given "Salty Whisker" is Released to "Tortuga"
        // When "Salty Whisker" arrives at "Tortuga"
        saltyWhiskerArrives(mittens, patch)

        // Then "Salty Whisker" is shown at "Tortuga" with "Mittens" with "Strong Paws" and "Patch" with "Sharp Eyes"
        crewOfSaltyWhisker() shouldBe listOf(
            Triple(mittens.first.toString(), "Mittens", "Strong Paws"),
            Triple(patch.first.toString(), "Patch", "Sharp Eyes"),
        )
    }

    @Test
    fun `An arriving Crew does not join the Recruit Pool`() {
        // Given the Recruit Pool of "Tortuga" before the Arrival
        val poolBefore = recruitPool()

        // When "Salty Whisker" arrives at "Tortuga"
        saltyWhiskerArrives(mittens, patch)

        // Then the Crew is aboard, but neither "Mittens" nor "Patch" is offered in the Recruit Pool of "Tortuga"
        crewOfSaltyWhisker().map { it.second } shouldBe listOf("Mittens", "Patch")
        val pool = recruitPool()
        pool.map { it["id"] }.none { it == mittens.first.toString() || it == patch.first.toString() } shouldBe true
        // And the Recruit Pool is unchanged
        pool shouldBe poolBefore
    }

    @Test
    fun `An Arrival received twice brings the Crew once`() {
        // When the Arrival of "Salty Whisker" at "Tortuga" is received twice: redelivered, and re-published
        val eventId = UUID.randomUUID()
        saltyWhiskerArrives(mittens, patch, eventId = eventId)
        saltyWhiskerArrives(mittens, patch, eventId = eventId)
        saltyWhiskerArrives(mittens, patch)

        // Then "Salty Whisker" has exactly 2 Crew Members at "Tortuga"
        crewOfSaltyWhisker() shouldBe listOf(
            Triple(mittens.first.toString(), "Mittens", "Strong Paws"),
            Triple(patch.first.toString(), "Patch", "Sharp Eyes"),
        )
    }

    @Test
    fun `A ship returning to a Harbor shows its current Crew`() {
        // Given "Salty Whisker" visited "Tortuga" with the Crew "Mittens" and "Patch"
        saltyWhiskerArrives(mittens, patch)
        crewOfSaltyWhisker().map { it.second } shouldBe listOf("Mittens", "Patch")
        // And it left again: Released from "Tortuga" to "Port Royal", which announced its Arrival
        restTemplate.postForEntity("/web/ships/$saltyWhiskerId/shippings", null, Map::class.java)
            .statusCode shouldBe HttpStatus.OK
        val release = restTemplate.release(saltyWhiskerId, "Port Royal")
        release.statusCode shouldBe HttpStatus.OK
        shippingEventListener.onShippingEvent(
            aShipArrivedRecord(
                shipId = saltyWhiskerId,
                shipName = "Salty Whisker",
                shippingId = UUID.fromString(release.body!!["id"] as String),
                originHarbor = "Tortuga",
                destinationHarbor = "Port Royal",
            )
        )
        restTemplate.getForEntity("/web/ships/$saltyWhiskerId", Map::class.java).statusCode shouldBe HttpStatus.NOT_FOUND

        // And "Salty Whisker" then Hired "Saffron" at "Port Royal" and is Released to "Tortuga" again
        // When "Salty Whisker" arrives at "Tortuga"
        saltyWhiskerArrives(mittens, patch, saffron)

        // Then the Crew of "Salty Whisker" at "Tortuga" is "Mittens", "Patch" and "Saffron"
        crewOfSaltyWhisker() shouldBe listOf(
            Triple(mittens.first.toString(), "Mittens", "Strong Paws"),
            Triple(patch.first.toString(), "Patch", "Sharp Eyes"),
            Triple(saffron.first.toString(), "Saffron", "Steady Hands"),
        )
    }

    private fun saltyWhiskerArrives(vararg crew: Triple<UUID, String, String>, eventId: UUID = UUID.randomUUID()) {
        shippingEventListener.onShippingEvent(
            aShippingPublishedRecord(
                shipId = saltyWhiskerId,
                shipName = "Salty Whisker",
                catainId = SeedData.aCatainId,
                originHarbor = "Port Royal",
                destinationHarbor = "Tortuga",
                homeHarbor = "Port Royal",
                eventId = eventId,
                crew = crew.toList(),
            )
        )
    }

    private fun crewOfSaltyWhisker(): List<Triple<String, String, String>> {
        val ship = getList("/web/ships").single { it["id"] == saltyWhiskerId.toString() }
        return (ship["crew"] as List<*>).map { it as Map<*, *> }
            .map { Triple(it["id"] as String, it["name"] as String, it["trait"] as String) }
    }

    private fun recruitPool(): List<Map<*, *>> = getList("/web/recruits")

    private fun getList(url: String): List<Map<*, *>> {
        val response = restTemplate.getForEntity(url, String::class.java)
        response.statusCode shouldBe HttpStatus.OK
        return ObjectMapper().readValue(response.body!!, List::class.java).map { it as Map<*, *> }
    }
}
