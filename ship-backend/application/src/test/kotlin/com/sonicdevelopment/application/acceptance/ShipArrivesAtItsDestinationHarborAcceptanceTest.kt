package com.sonicdevelopment.application.acceptance

import com.fasterxml.jackson.databind.ObjectMapper
import com.sonicdevelopment.application.KafkaTestcontainer
import com.sonicdevelopment.application.PostgresTestcontainer
import com.sonicdevelopment.application.acceptance.fixtures.STARTING_STOCK
import com.sonicdevelopment.application.acceptance.fixtures.aShippingPublishedRecord
import com.sonicdevelopment.application.acceptance.fixtures.cargoIdOf
import com.sonicdevelopment.application.acceptance.fixtures.givenStockOf
import com.sonicdevelopment.application.acceptance.fixtures.resetStockToStartingStock
import com.sonicdevelopment.application.acceptance.fixtures.truncateMutableTables
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
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
 * Feature: A ship arrives at its Destination Harbor.
 *
 * The Release at "Tortuga" is simulated by producing the `shipping-published` record Debezium would
 * relay from Tortuga's outbox. "Arrives" means the Harbor has consumed it (its event id is in the
 * inbox). The seeded Catain "Catain Black Whiskers" stands in for "Whiskers".
 */
class ShipArrivesAtItsDestinationHarborAcceptanceTest {

    @Nested
    @TestPropertySource(properties = ["harbor.name=Port Royal"])
    inner class AtPortRoyal : AHarbor() {

        @Test
        fun `The Cargo of an arriving ship is unloaded into the Stock`() {
            // Given the Stock of "Port Royal" holds 1 "Rum" and no "Silk"
            jdbcTemplate.givenStockOf("Rum", 1)
            jdbcTemplate.givenStockOf("Silk", 0)

            // When "Black Pearl" arrives at "Port Royal"
            awaitConsumed(blackPearlIsAnnounced())

            // Then the Stock of "Port Royal" holds 2 "Rum" and 1 "Silk"
            stock()["Rum"] shouldBe 2
            stock()["Silk"] shouldBe 1
        }

        @Test
        fun `The arrived ship joins the fleet of the Destination Harbor`() {
            // When "Black Pearl" arrives at "Port Royal"
            awaitConsumed(blackPearlIsAnnounced())

            // Then "Black Pearl" with Catain "Whiskers" and its Ship Id is among the Available Ships of "Port Royal"
            val ship = availableShips().single { it["id"] == blackPearlId.toString() }
            ship["name"] shouldBe "Black Pearl"
            ship["catain"] shouldBe WHISKERS
            // And it has no Loaded Cargo and no Active Shipping
            (ship["shippingState"] == null || ship["shippingState"] == "IDLE") shouldBe true
            (shipDetails(blackPearlId)["cargo"] as List<*>).shouldBeEmpty()
        }

        @Test
        fun `The Destination Harbor announces the Arrival`() {
            // When "Black Pearl" arrives at "Port Royal"
            awaitConsumed(blackPearlIsAnnounced())

            // Then "Port Royal" announces Ship Arrived for "Black Pearl" to "Tortuga"
            val rows = shipArrivedRows()
            rows shouldHaveSize 1
            rows.single()["aggregate_type"] shouldBe "shipping"
            rows.single()["aggregate_id"] shouldBe blackPearlShippingId
            val payload = ObjectMapper().readTree(rows.single()["payload"] as String)
            payload["shipId"].asText() shouldBe blackPearlId.toString()
            payload["shipName"].asText() shouldBe "Black Pearl"
            payload["originHarbor"].asText() shouldBe "Tortuga"
            payload["destinationHarbor"].asText() shouldBe "Port Royal"
        }

        @Test
        fun `An Arrival announced twice is handled once`() {
            // Given "Black Pearl" has already arrived at "Port Royal"
            val firstAnnouncement = blackPearlIsAnnounced()
            awaitConsumed(firstAnnouncement)
            val stockAfterArrival = stock()

            // When the Release of "Black Pearl" is announced to "Port Royal" again:
            // redelivered with the same event id, and re-published under a new one
            blackPearlIsAnnounced(eventId = firstAnnouncement)
            val republished = blackPearlIsAnnounced()
            awaitConsumed(republished) // one partition: the redelivery has been handled before it

            // Then the Stock of "Port Royal" is unchanged
            stock() shouldBe stockAfterArrival
            // And "Black Pearl" appears exactly once among the Available Ships of "Port Royal"
            availableShips().count { it["id"] == blackPearlId.toString() } shouldBe 1
            shipArrivedRows() shouldHaveSize 1
        }

        @Test
        fun `A failed Arrival leaves no trace, not even in the inbox`() {
            // Given a ship whose Catain is unknown at "Port Royal" is announced to "Port Royal"
            val unknownCatain = aShippingPublishedRecord(
                shipName = "Ghost Ship",
                catainId = UUID.randomUUID(),
                cargoIds = listOf(jdbcTemplate.cargoIdOf("Rum")),
                originHarbor = "Tortuga",
                destinationHarbor = "Port Royal",
            )
            val failedEventId = UUID.fromString(String(unknownCatain.headers().lastHeader("id").value()))
            kafkaTemplate.send(unknownCatain).get()

            // When the Harbor has given up on it: a later record on the single partition is consumed
            // (the default error handler retries a failing record a few times without delay, then skips it)
            val marker = aShippingPublishedRecord(
                catainId = whiskersId(),
                originHarbor = "Tortuga",
                destinationHarbor = "Nassau",
            )
            kafkaTemplate.send(marker).get()
            awaitConsumed(UUID.fromString(String(marker.headers().lastHeader("id").value())))

            // Then the inbox holds no row for the failed event, so a redelivery is not dropped as a duplicate
            jdbcTemplate.queryForObject(
                "SELECT count(*) FROM inbox_events WHERE event_id = ?", Int::class.java, failedEventId
            ) shouldBe 0
            // And the Stock and the fleet of "Port Royal" are unchanged
            stock().values.forEach { it shouldBe STARTING_STOCK }
            availableShips().shouldBeEmpty()
            shipArrivedRows().shouldBeEmpty()
        }

        @Test
        fun `A Harbor ignores ships sailing to another Harbor`() {
            // Given the ship "Flying Dutchman" was Released at "Tortuga" to "Nassau"
            val flyingDutchman = aShippingPublishedRecord(
                shipName = "Flying Dutchman",
                catainId = whiskersId(),
                cargoIds = listOf(jdbcTemplate.cargoIdOf("Rum")),
                originHarbor = "Tortuga",
                destinationHarbor = "Nassau",
            )

            // When the Release of "Flying Dutchman" is announced to "Port Royal"
            kafkaTemplate.send(flyingDutchman).get()

            // Then the Stock and the fleet of "Port Royal" are unchanged
            awaitConsumed(UUID.fromString(String(flyingDutchman.headers().lastHeader("id").value())))
            stock().values.forEach { it shouldBe STARTING_STOCK }
            availableShips().shouldBeEmpty()
            shipArrivedRows().shouldBeEmpty()
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

        /** Background: "Black Pearl" with Catain "Whiskers", Released at "Tortuga" to "Port Royal" with "Rum" and "Silk". */
        val blackPearlId: UUID = UUID.randomUUID()
        val blackPearlShippingId: UUID = UUID.randomUUID()

        @BeforeEach
        fun resetHarbor() {
            jdbcTemplate.truncateMutableTables()
            jdbcTemplate.resetStockToStartingStock()
        }

        /** Produces the Shipping Published of "Black Pearl" for "Port Royal" and returns its event id. */
        fun blackPearlIsAnnounced(eventId: UUID = UUID.randomUUID()): UUID {
            kafkaTemplate.send(
                aShippingPublishedRecord(
                    shipId = blackPearlId,
                    shipName = "Black Pearl",
                    catainId = whiskersId(),
                    shippingId = blackPearlShippingId,
                    cargoIds = listOf(jdbcTemplate.cargoIdOf("Rum"), jdbcTemplate.cargoIdOf("Silk")),
                    originHarbor = "Tortuga",
                    destinationHarbor = "Port Royal",
                    eventId = eventId,
                )
            ).get()
            return eventId
        }

        fun whiskersId(): UUID =
            jdbcTemplate.queryForObject("SELECT catain_id FROM catains WHERE catain_name = ?", UUID::class.java, WHISKERS)!!

        fun awaitConsumed(eventId: UUID) {
            await().atMost(Duration.ofSeconds(30)).untilAsserted {
                jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM inbox_events WHERE event_id = ?", Int::class.java, eventId
                ) shouldBe 1
            }
        }

        /** The Stock as the User sees it among the Available Cargo; a Cargo out of Stock counts 0. */
        fun stock(): Map<String, Int> {
            val response = restTemplate.getForEntity("/web/cargos", List::class.java)
            response.statusCode shouldBe HttpStatus.OK
            val offered = response.body!!.map { it as Map<*, *> }.associate { it["name"] as String to it["stock"] as Int }
            val catalog = jdbcTemplate.queryForList("SELECT cargo_name FROM cargos", String::class.java)
            return catalog.associateWith { offered[it] ?: 0 }
        }

        fun availableShips(): List<Map<*, *>> {
            val response = restTemplate.getForEntity("/web/ships", List::class.java)
            response.statusCode shouldBe HttpStatus.OK
            return response.body!!.map { it as Map<*, *> }
        }

        fun shipDetails(shipId: UUID): Map<*, *> {
            val response = restTemplate.getForEntity("/web/ships/$shipId", Map::class.java)
            response.statusCode shouldBe HttpStatus.OK
            return response.body!!
        }

        fun shipArrivedRows(): List<Map<String, Any>> =
            jdbcTemplate.queryForList(
                "SELECT aggregate_type, aggregate_id, payload FROM shipping_outbox WHERE event_type = 'ship-arrived'"
            )
    }

    private companion object {
        const val WHISKERS = "Catain Black Whiskers"
    }
}
