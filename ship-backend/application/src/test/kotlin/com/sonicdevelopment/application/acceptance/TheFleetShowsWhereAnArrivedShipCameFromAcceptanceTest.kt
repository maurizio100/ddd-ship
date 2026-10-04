package com.sonicdevelopment.application.acceptance

import com.sonicdevelopment.application.KafkaTestcontainer
import com.sonicdevelopment.application.PostgresTestcontainer
import com.sonicdevelopment.application.acceptance.fixtures.aShipArrivedRecord
import com.sonicdevelopment.application.acceptance.fixtures.aShippingPublishedRecord
import com.sonicdevelopment.application.acceptance.fixtures.givenKnownHarbors
import com.sonicdevelopment.application.acceptance.fixtures.release
import com.sonicdevelopment.application.acceptance.fixtures.resetStockToStartingStock
import com.sonicdevelopment.application.acceptance.fixtures.truncateMutableTables
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
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource
import java.time.Duration
import java.util.*

/**
 * Feature: The fleet shows where an arrived ship came from.
 *
 * The scenarios run at "Port Royal". An Arrival is simulated by producing the `shipping-published` record
 * Debezium would relay from the Origin Harbor's outbox; "arrived" means Port Royal has consumed it.
 */
class TheFleetShowsWhereAnArrivedShipCameFromAcceptanceTest {

    @Nested
    @TestPropertySource(properties = ["harbor.name=Port Royal"])
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @Import(PostgresTestcontainer::class, KafkaTestcontainer::class)
    @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
    inner class AtPortRoyal {

        @Autowired
        lateinit var restTemplate: TestRestTemplate

        @Autowired
        lateinit var jdbcTemplate: JdbcTemplate

        @Autowired
        lateinit var kafkaTemplate: KafkaTemplate<String, String>

        private val blackPearlId: UUID = UUID.randomUUID()

        @BeforeEach
        fun resetHarbor() {
            jdbcTemplate.truncateMutableTables()
            jdbcTemplate.resetStockToStartingStock()
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
                "/web/ships", mapOf("name" to "Interceptor", "catainId" to seededCatainId()), Map::class.java
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
            jdbcTemplate.givenKnownHarbors("Nassau")
            restTemplate.postForEntity("/web/ships/$blackPearlId/shippings", null, Map::class.java)
                .statusCode shouldBe HttpStatus.OK
            val toNassau = restTemplate.release(blackPearlId, "Nassau")
            toNassau.statusCode shouldBe HttpStatus.OK
            val arrivedAtNassau = UUID.randomUUID()
            kafkaTemplate.send(
                aShipArrivedRecord(
                    shipId = blackPearlId,
                    shippingId = UUID.fromString(toNassau.body!!["id"] as String),
                    originHarbor = "Port Royal",
                    destinationHarbor = "Nassau",
                    eventId = arrivedAtNassau,
                )
            ).get()
            awaitConsumed(arrivedAtNassau)
            availableShips().none { it["id"] == blackPearlId.toString() } shouldBe true

            // And "Black Pearl" later arrived at "Port Royal" again from "Nassau"
            blackPearlArrivesFrom("Nassau")

            // Then "Port Royal" lists "Black Pearl" as arrived from "Nassau"
            fleetEntryOf(blackPearlId)["arrivedFrom"] shouldBe "Nassau"
            detailsOf(blackPearlId)["arrivedFrom"] shouldBe "Nassau"
        }

        /** Produces the Origin Harbor's Shipping Published of "Black Pearl" to "Port Royal" and awaits it. */
        private fun blackPearlArrivesFrom(originHarbor: String) {
            val eventId = UUID.randomUUID()
            kafkaTemplate.send(
                aShippingPublishedRecord(
                    shipId = blackPearlId,
                    shipName = "Black Pearl",
                    catainId = seededCatainId(),
                    shippingId = UUID.randomUUID(),
                    originHarbor = originHarbor,
                    destinationHarbor = "Port Royal",
                    eventId = eventId,
                )
            ).get()
            awaitConsumed(eventId)
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

        private fun seededCatainId(): UUID =
            jdbcTemplate.queryForObject("SELECT catain_id FROM catains ORDER BY id LIMIT 1", UUID::class.java)!!

        private fun awaitConsumed(eventId: UUID) {
            await().atMost(Duration.ofSeconds(30)).untilAsserted {
                jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM inbox_events WHERE event_id = ?", Int::class.java, eventId
                ) shouldBe 1
            }
        }
    }
}
