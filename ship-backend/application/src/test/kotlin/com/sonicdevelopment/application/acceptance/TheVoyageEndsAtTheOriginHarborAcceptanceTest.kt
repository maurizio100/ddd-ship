package com.sonicdevelopment.application.acceptance

import com.sonicdevelopment.application.KafkaTestcontainer
import com.sonicdevelopment.application.PostgresTestcontainer
import com.sonicdevelopment.application.acceptance.fixtures.aShipArrivedRecord
import com.sonicdevelopment.application.acceptance.fixtures.aShipBeingPrepared
import com.sonicdevelopment.application.acceptance.fixtures.aShippingPublishedRecord
import com.sonicdevelopment.application.acceptance.fixtures.givenKnownHarbors
import com.sonicdevelopment.application.acceptance.fixtures.release
import com.sonicdevelopment.application.acceptance.fixtures.resetStockToStartingStock
import com.sonicdevelopment.application.acceptance.fixtures.truncateMutableTables
import io.kotest.matchers.collections.shouldContainAll
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
 * Feature: The voyage ends at the Origin Harbor.
 *
 * At "Tortuga" the Release is real (over HTTP); the Arrival at "Port Royal" is simulated by producing
 * the `ship-arrived` record Debezium would relay from Port Royal's outbox. At "Port Royal" the Arrival
 * itself is simulated by producing Tortuga's `shipping-published`. "Learns" means the Harbor has consumed
 * the event (its event id is in the inbox).
 */
class TheVoyageEndsAtTheOriginHarborAcceptanceTest {

    @Nested
    @TestPropertySource(properties = ["harbor.name=Tortuga"])
    inner class AtTortuga : AHarbor() {

        lateinit var blackPearlId: UUID
        lateinit var blackPearlShippingId: UUID

        /** Background: "Black Pearl" was Released at "Tortuga" to "Port Royal". */
        @BeforeEach
        fun blackPearlIsReleasedToPortRoyal() {
            jdbcTemplate.givenKnownHarbors("Port Royal")
            blackPearlId = restTemplate.aShipBeingPrepared(jdbcTemplate, "Black Pearl")
            val release = restTemplate.release(blackPearlId, "Port Royal")
            release.statusCode shouldBe HttpStatus.OK
            blackPearlShippingId = UUID.fromString(release.body!!["id"] as String)
        }

        @Test
        fun `The ship stays at sea until it has arrived`() {
            // Then "Tortuga" lists "Black Pearl" as at sea
            val ship = availableShips().single { it["id"] == blackPearlId.toString() }
            ship["name"] shouldBe "Black Pearl"
            ship["shippingState"] shouldBe "SHIPPING"
        }

        @Test
        fun `The Shipping is done once the ship has arrived`() {
            // When "Tortuga" learns that "Black Pearl" arrived at "Port Royal"
            awaitConsumed(blackPearlArrivedAtPortRoyal())

            // Then the Shipping of "Black Pearl" is done
            shippingStateOfBlackPearlsVoyage() shouldBe "DONE"
            // And "Black Pearl" is no longer among the Available Ships of "Tortuga"
            availableShips().none { it["id"] == blackPearlId.toString() } shouldBe true
            restTemplate.getForEntity("/web/ships/$blackPearlId", Map::class.java).statusCode shouldBe
                HttpStatus.NOT_FOUND
        }

        @Test
        fun `An Arrival reported twice is handled once`() {
            // Given "Tortuga" has learned that "Black Pearl" arrived at "Port Royal"
            val firstReport = blackPearlArrivedAtPortRoyal()
            awaitConsumed(firstReport)

            // When "Tortuga" learns it again: redelivered with the same event id, and re-published under a new one
            blackPearlArrivedAtPortRoyal(eventId = firstReport)
            val republished = blackPearlArrivedAtPortRoyal()
            awaitConsumed(republished) // one partition: the redelivery has been handled before it

            // Then the Shipping of "Black Pearl" is still done
            shippingStateOfBlackPearlsVoyage() shouldBe "DONE"
            availableShips().none { it["id"] == blackPearlId.toString() } shouldBe true
            // And neither report was left failing: both are recorded as consumed
            jdbcTemplate.queryForList("SELECT event_id FROM inbox_events", UUID::class.java) shouldContainAll
                listOf(firstReport, republished)
        }

        @Test
        fun `A ship that left the fleet is taken in again when it returns`() {
            // Given "Black Pearl" has arrived at "Port Royal" and "Tortuga" has learned of it
            awaitConsumed(blackPearlArrivedAtPortRoyal())

            // When "Black Pearl" is Released at "Port Royal" back to "Tortuga" and arrives
            val returnRelease = aShippingPublishedRecord(
                shipId = blackPearlId,
                shipName = "Black Pearl",
                catainId = seededCatainId(),
                shippingId = UUID.randomUUID(),
                originHarbor = "Port Royal",
                destinationHarbor = "Tortuga",
            )
            kafkaTemplate.send(returnRelease).get()
            awaitConsumed(eventIdOf(returnRelease.headers().lastHeader("id").value()))

            // Then "Black Pearl" appears exactly once among the Available Ships, with no Active Shipping
            val ships = availableShips().filter { it["id"] == blackPearlId.toString() }
            ships.size shouldBe 1
            (ships.single()["shippingState"] == null || ships.single()["shippingState"] == "IDLE") shouldBe true
            // And its first Shipping is still readable as done
            shippingStateOfBlackPearlsVoyage() shouldBe "DONE"
        }

        /** Produces Port Royal's Ship Arrived for "Black Pearl" and returns its event id. */
        fun blackPearlArrivedAtPortRoyal(eventId: UUID = UUID.randomUUID()): UUID {
            kafkaTemplate.send(
                aShipArrivedRecord(
                    shipId = blackPearlId,
                    shipName = "Black Pearl",
                    shippingId = blackPearlShippingId,
                    originHarbor = "Tortuga",
                    destinationHarbor = "Port Royal",
                    eventId = eventId,
                )
            ).get()
            return eventId
        }

        fun shippingStateOfBlackPearlsVoyage(): Any? {
            val response = restTemplate.getForEntity(
                "/web/ships/$blackPearlId/shippings/$blackPearlShippingId", Map::class.java
            )
            response.statusCode shouldBe HttpStatus.OK
            return response.body!!["shippingState"]
        }
    }

    @Nested
    @TestPropertySource(properties = ["harbor.name=Port Royal"])
    inner class AtPortRoyal : AHarbor() {

        private val blackPearlId: UUID = UUID.randomUUID()

        /** Background: "Black Pearl" was Released at "Tortuga" to "Port Royal". */
        @BeforeEach
        fun blackPearlIsReleasedAtTortugaToPortRoyal() {
            val release = aShippingPublishedRecord(
                shipId = blackPearlId,
                shipName = "Black Pearl",
                catainId = seededCatainId(),
                originHarbor = "Tortuga",
                destinationHarbor = "Port Royal",
            )
            kafkaTemplate.send(release).get()
            awaitConsumed(eventIdOf(release.headers().lastHeader("id").value()))
        }

        @Test
        fun `An arrived ship can sail back`() {
            // Given "Black Pearl" has arrived at "Port Royal"
            jdbcTemplate.givenKnownHarbors("Tortuga")

            // When the User at "Port Royal" Releases "Black Pearl" on a new Shipping to "Tortuga"
            restTemplate.postForEntity("/web/ships/$blackPearlId/shippings", null, Map::class.java)
                .statusCode shouldBe HttpStatus.OK
            val release = restTemplate.release(blackPearlId, "Tortuga")

            // Then "Black Pearl" is at sea with Destination Harbor "Tortuga"
            release.statusCode shouldBe HttpStatus.OK
            release.body!!["destinationHarbor"] shouldBe "Tortuga"
            availableShips().single { it["id"] == blackPearlId.toString() }["shippingState"] shouldBe "SHIPPING"
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
        fun resetHarbor() {
            jdbcTemplate.truncateMutableTables()
            jdbcTemplate.resetStockToStartingStock()
        }

        fun seededCatainId(): UUID =
            jdbcTemplate.queryForObject("SELECT catain_id FROM catains ORDER BY id LIMIT 1", UUID::class.java)!!

        fun eventIdOf(header: ByteArray): UUID = UUID.fromString(String(header, Charsets.UTF_8))

        fun awaitConsumed(eventId: UUID) {
            await().atMost(Duration.ofSeconds(30)).untilAsserted {
                jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM inbox_events WHERE event_id = ?", Int::class.java, eventId
                ) shouldBe 1
            }
        }

        fun availableShips(): List<Map<*, *>> {
            val response = restTemplate.getForEntity("/web/ships", List::class.java)
            response.statusCode shouldBe HttpStatus.OK
            return response.body!!.map { it as Map<*, *> }
        }
    }
}
