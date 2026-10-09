package com.sonicdevelopment.application.acceptance

import com.sonicdevelopment.application.acceptance.fixtures.FakeDrivenPorts
import com.sonicdevelopment.application.acceptance.fixtures.FakeHarborTest
import com.sonicdevelopment.application.acceptance.fixtures.aHarborOpenedRecord
import com.sonicdevelopment.driving.adapter.messaging.HarborEventListener
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestPropertySource
import java.util.*

/**
 * Feature: Harbors learn about each other.
 *
 * The Harbor is "Tortuga", on in-memory fakes. Another Harbor "opens" by handing the listener the record
 * Debezium would relay from its outbox; the delivery is synchronous.
 */
@FakeHarborTest
@TestPropertySource(properties = ["harbor.name=Tortuga"])
class HarborsLearnAboutEachOtherAcceptanceTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var fakes: FakeDrivenPorts

    @Autowired
    lateinit var harborEventListener: HarborEventListener

    @BeforeEach
    fun resetHarbor() {
        fakes.reset()
    }

    @Test
    fun `A Harbor that opens becomes known to the open Harbors`() {
        // Given the Harbor "Tortuga" is open (this context)

        // When the Harbor "Port Royal" opens
        harborOpens("Port Royal")

        // Then "Port Royal" is one of the Known Harbors of "Tortuga"
        knownHarbors().knownHarbors shouldContain "Port Royal"
    }

    @Test
    fun `A Harbor is not one of its own Known Harbors`() {
        // When the Harbor "Tortuga" opens (its own Harbor Opened, relayed back by Debezium)
        harborOpens("Tortuga")

        // Then "Tortuga" is not one of the Known Harbors of "Tortuga"
        val harbors = knownHarbors()
        harbors.harborName shouldBe "Tortuga"
        harbors.knownHarbors shouldNotContain "Tortuga"
    }

    @Test
    fun `A Harbor that opens again is known only once`() {
        // Given "Tortuga" knows the Harbor "Port Royal"
        harborOpens("Port Royal")
        knownHarbors().knownHarbors shouldContain "Port Royal"

        // When "Port Royal" opens again
        harborOpens("Port Royal")

        // Then "Port Royal" appears exactly once among the Known Harbors of "Tortuga"
        knownHarbors().knownHarbors.count { it == "Port Royal" } shouldBe 1
    }

    /** Hands the listener the Harbor Opened of [harborName]. */
    private fun harborOpens(harborName: String) {
        harborEventListener.onHarborEvent(aHarborOpenedRecord(harborName, UUID.randomUUID()))
    }

    private fun knownHarbors(): KnownHarbors {
        val response = restTemplate.getForEntity("/web/harbors", KnownHarbors::class.java)
        response.statusCode shouldBe HttpStatus.OK
        return response.body!!
    }

    data class KnownHarbors(val harborName: String, val knownHarbors: List<String>)
}
