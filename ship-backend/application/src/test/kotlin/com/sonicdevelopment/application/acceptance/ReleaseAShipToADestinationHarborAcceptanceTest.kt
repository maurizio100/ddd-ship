package com.sonicdevelopment.application.acceptance

import com.sonicdevelopment.application.acceptance.fixtures.FakeDrivenPorts
import com.sonicdevelopment.application.acceptance.fixtures.FakeHarborTest
import com.sonicdevelopment.application.acceptance.fixtures.OutboxMessage
import com.sonicdevelopment.application.acceptance.fixtures.aShipBeingPrepared
import com.sonicdevelopment.application.acceptance.fixtures.givenKnownHarbors
import com.sonicdevelopment.application.acceptance.fixtures.release
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import java.util.*

/**
 * Feature: Release a ship to a Destination Harbor.
 *
 * The Harbor is "Tortuga", on in-memory fakes. Its Known Harbors are given directly, as if learned from Harbor Opened
 * (STORY-003); the Destination Harbor choices are what the User sees at `GET /web/harbors`.
 */
@FakeHarborTest
@TestPropertySource(properties = ["harbor.name=Tortuga"])
class ReleaseAShipToADestinationHarborAcceptanceTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var fakes: FakeDrivenPorts

    @BeforeEach
    fun resetHarbor() {
        fakes.reset()
    }

    @Test
    fun `The Destination Harbor choices are the Known Harbors`() {
        // Given "Tortuga" knows the Harbors "Port Royal" and "Nassau"
        fakes.givenKnownHarbors("Port Royal", "Nassau")
        // And a ship at "Tortuga" is being prepared
        restTemplate.aShipBeingPrepared()

        // Then the Destination Harbor choices for the ship are "Port Royal" and "Nassau"
        destinationHarborChoices() shouldBe listOf("Nassau", "Port Royal")
    }

    @Test
    fun `A ship is Released to a Known Harbor`() {
        // Given "Tortuga" knows the Harbor "Port Royal"
        fakes.givenKnownHarbors("Port Royal")
        // And a ship at "Tortuga" is being prepared
        val shipId = restTemplate.aShipBeingPrepared()

        // When the User Releases the ship to "Port Royal"
        val release = restTemplate.release(shipId, "Port Royal")

        // Then the ship is at sea with Destination Harbor "Port Royal"
        release.statusCode shouldBe HttpStatus.OK
        release.body!!["destinationHarbor"] shouldBe "Port Royal"
        shippingStateOf(shipId) shouldBe "SHIPPING"
        // And the Shipping Published names "Tortuga" as Origin Harbor and "Port Royal" as Destination Harbor
        val payloads = shippingPublishedPayloads()
        payloads.size shouldBe 1
        val published = payloads.single()
        published.originHarbor shouldBe "Tortuga"
        published.destinationHarbor shouldBe "Port Royal"
        published.shipId shouldBe shipId
    }

    @Test
    fun `A ship cannot be Released to an unknown Harbor`() {
        // Given "Tortuga" does not know the Harbor "Atlantis"
        fakes.givenKnownHarbors("Port Royal")
        // And a ship at "Tortuga" is being prepared
        val shipId = restTemplate.aShipBeingPrepared()

        // When the User Releases the ship to "Atlantis"
        val release = restTemplate.release(shipId, "Atlantis")

        // Then the Release is rejected
        release.statusCode shouldBe HttpStatus.CONFLICT
        release.headers.contentType?.isCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON) shouldBe true
        release.body!!["detail"] shouldBe "Atlantis is not a Known Harbor"
        // And the ship is still being prepared
        shippingStateOf(shipId) shouldBe "PREPARING"
        shippingPublishedPayloads().shouldBeEmpty()
    }

    @Test
    fun `A ship cannot be Released without a Known Harbor`() {
        // Given "Tortuga" knows no other Harbor
        // And a ship at "Tortuga" is being prepared
        val shipId = restTemplate.aShipBeingPrepared()

        // Then the ship cannot be Released
        destinationHarborChoices().shouldBeEmpty()
        val release = restTemplate.release(shipId, "Port Royal")
        release.statusCode shouldBe HttpStatus.CONFLICT
        shippingStateOf(shipId) shouldBe "PREPARING"
        shippingPublishedPayloads().shouldBeEmpty()
    }

    @Test
    fun `A ship at sea cannot be Released again`() {
        // Given "Tortuga" knows the Harbors "Port Royal" and "Nassau"
        fakes.givenKnownHarbors("Port Royal", "Nassau")
        // And a ship at "Tortuga" has been Released to "Port Royal"
        val shipId = restTemplate.aShipBeingPrepared("Black Pearl")
        restTemplate.release(shipId, "Port Royal").statusCode shouldBe HttpStatus.OK

        // When the User Releases the ship to "Nassau"
        val release = restTemplate.release(shipId, "Nassau")

        // Then the Release is rejected
        release.statusCode shouldBe HttpStatus.CONFLICT
        release.headers.contentType?.isCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON) shouldBe true
        release.body!!["detail"] shouldBe "Black Pearl is not being prepared"
        // And the ship is still bound for "Port Royal" and only one Shipping Published exists
        shippingStateOf(shipId) shouldBe "SHIPPING"
        val payloads = shippingPublishedPayloads()
        payloads.size shouldBe 1
        payloads.single().destinationHarbor shouldBe "Port Royal"
    }

    private fun destinationHarborChoices(): List<String> {
        val harbors = restTemplate.getForEntity("/web/harbors", Map::class.java)
        harbors.statusCode shouldBe HttpStatus.OK
        return (harbors.body!!["knownHarbors"] as List<*>).map { it as String }
    }

    private fun shippingStateOf(shipId: UUID): String? {
        val ships = restTemplate.getForEntity("/web/ships", List::class.java)
        ships.statusCode shouldBe HttpStatus.OK
        return ships.body!!.map { it as Map<*, *> }.single { it["id"] == shipId.toString() }["shippingState"] as String?
    }

    private fun shippingPublishedPayloads(): List<OutboxMessage.ShippingPublished> = fakes.outbox.shippingPublished()
}
