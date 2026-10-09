package com.sonicdevelopment.application.acceptance

import com.sonicdevelopment.application.DbTest
import com.sonicdevelopment.application.KafkaTestcontainer
import com.sonicdevelopment.application.PostgresTestcontainer
import com.sonicdevelopment.application.acceptance.fixtures.aHarborOpenedRecord
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource
import java.time.Duration
import java.util.*

/**
 * Feature: Harbors learn about each other.
 *
 * Another Harbor "opens" by producing the record Debezium would relay from its outbox. Each nested
 * class is one Harbor with its own application context, closed after the class, so two Harbor
 * listeners never run at once.
 */
@DbTest
class HarborsLearnAboutEachOtherAcceptanceTest {

    @Nested
    @TestPropertySource(properties = ["harbor.name=Tortuga"])
    inner class AtTortuga : AHarbor() {

        @Test
        fun `A Harbor that opens becomes known to the open Harbors`() {
            // Given the Harbor "Tortuga" is open (this context)

            // When the Harbor "Port Royal" opens
            val portRoyalOpened = harborOpens("Port Royal")

            // Then "Port Royal" is one of the Known Harbors of "Tortuga"
            awaitConsumed(portRoyalOpened)
            knownHarbors().knownHarbors shouldContain "Port Royal"
        }

        @Test
        fun `A Harbor is not one of its own Known Harbors`() {
            // When the Harbor "Tortuga" opens (its own Harbor Opened, relayed back by Debezium)
            val tortugaOpened = harborOpens("Tortuga")

            // Then "Tortuga" is not one of the Known Harbors of "Tortuga"
            awaitConsumed(tortugaOpened)
            val harbors = knownHarbors()
            harbors.harborName shouldBe "Tortuga"
            harbors.knownHarbors shouldNotContain "Tortuga"
        }

        @Test
        fun `A Harbor that opens again is known only once`() {
            // Given "Tortuga" knows the Harbor "Port Royal"
            awaitConsumed(harborOpens("Port Royal"))
            knownHarbors().knownHarbors shouldContain "Port Royal"

            // When "Port Royal" opens again
            val portRoyalOpenedAgain = harborOpens("Port Royal")

            // Then "Port Royal" appears exactly once among the Known Harbors of "Tortuga"
            awaitConsumed(portRoyalOpenedAgain)
            knownHarbors().knownHarbors.count { it == "Port Royal" } shouldBe 1
        }
    }

    @Nested
    @TestPropertySource(properties = ["harbor.name=Port Royal", "spring.kafka.listener.auto-startup=false"])
    inner class AtPortRoyal : AHarbor() {

        @Autowired
        lateinit var listenerRegistry: KafkaListenerEndpointRegistry

        @Test
        fun `An opening Harbor learns about the Harbors opened before it`() {
            // Given the Harbors "Tortuga" and "Nassau" are open
            val tortugaOpened = harborOpens("Tortuga")
            val nassauOpened = harborOpens("Nassau")

            // When the Harbor "Port Royal" opens
            listenerRegistry.getListenerContainer(HARBOR_LISTENER_ID)!!.start()

            // Then the Known Harbors of "Port Royal" are "Tortuga" and "Nassau"
            awaitConsumed(tortugaOpened)
            awaitConsumed(nassauOpened)
            knownHarbors().knownHarbors shouldContainExactlyInAnyOrder listOf("Tortuga", "Nassau")
        }
    }

    /** One running Harbor: the app against the shared Postgres and Kafka. */
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @Import(PostgresTestcontainer::class, KafkaTestcontainer::class)
    @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
    abstract class AHarbor {

        @Autowired
        lateinit var restTemplate: TestRestTemplate

        @Autowired
        lateinit var jdbcTemplate: JdbcTemplate

        @Autowired
        lateinit var kafkaTemplate: KafkaTemplate<String, String>

        @BeforeEach
        fun truncateMutableTables() {
            jdbcTemplate.execute("TRUNCATE TABLE known_harbors, inbox_events, shipping_outbox")
        }

        /** Produces the Harbor Opened of [harborName] and returns its event id. */
        fun harborOpens(harborName: String): UUID {
            val eventId = UUID.randomUUID()
            kafkaTemplate.send(aHarborOpenedRecord(harborName, eventId)).get()
            return eventId
        }

        fun awaitConsumed(eventId: UUID) {
            await().atMost(Duration.ofSeconds(30)).untilAsserted {
                jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM inbox_events WHERE event_id = ?", Int::class.java, eventId
                ) shouldBe 1
            }
        }

        fun knownHarbors(): KnownHarbors {
            val response = restTemplate.getForEntity("/web/harbors", KnownHarbors::class.java)
            response.statusCode shouldBe HttpStatus.OK
            return response.body!!
        }
    }

    data class KnownHarbors(val harborName: String, val knownHarbors: List<String>)

    private companion object {
        const val HARBOR_LISTENER_ID = "harbor-events"
    }
}
