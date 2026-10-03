package com.sonicdevelopment.application.acceptance

import com.sonicdevelopment.application.KafkaTestcontainer
import com.sonicdevelopment.application.PostgresTestcontainer
import com.sonicdevelopment.application.acceptance.fixtures.FleetEvent
import com.sonicdevelopment.application.acceptance.fixtures.FleetEventStream
import com.sonicdevelopment.application.acceptance.fixtures.aShipArrivedRecord
import com.sonicdevelopment.application.acceptance.fixtures.aShipBeingPrepared
import com.sonicdevelopment.application.acceptance.fixtures.aShippingPublishedRecord
import com.sonicdevelopment.application.acceptance.fixtures.givenKnownHarbors
import com.sonicdevelopment.application.acceptance.fixtures.release
import com.sonicdevelopment.application.acceptance.fixtures.resetStockToStartingStock
import com.sonicdevelopment.application.acceptance.fixtures.truncateMutableTables
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource
import java.time.Duration
import java.util.*

/**
 * Feature: A Harbor sees ships arrive and leave as they happen.
 *
 * "The User is looking at the fleet" means the User's browser has the fleet-events stream
 * (`GET /web/fleet-events`) open, as the Available Ships page does; what it is pushed is what the User sees
 * change. Arrivals and Ship Arrived are simulated by producing the records Debezium would relay from the
 * other Harbor's outbox.
 */
class AHarborSeesShipsArriveAndLeaveAcceptanceTest {

    @Nested
    @TestPropertySource(properties = ["harbor.name=Port Royal"])
    inner class AtPortRoyal : AHarbor() {

        @Test
        fun `An arriving ship appears without a reload`() {
            // Given the User at "Port Royal" is looking at the fleet
            val blackPearlId = UUID.randomUUID()

            // When "Black Pearl" arrives at "Port Royal" from "Tortuga"
            kafkaTemplate.send(
                aShippingPublishedRecord(
                    shipId = blackPearlId,
                    shipName = "Black Pearl",
                    catainId = seededCatainId(),
                    originHarbor = "Tortuga",
                    destinationHarbor = "Port Royal",
                )
            ).get()

            // Then the User is told that "Black Pearl" arrived from "Tortuga"
            val pushed = fleet.nextAbout("ship-arrived", blackPearlId)
            pushed.data["shipName"] shouldBe "Black Pearl"
            pushed.data["originHarbor"] shouldBe "Tortuga"
            // And "Black Pearl" appears among the Available Ships of "Port Royal": the push came after the commit,
            // so the refetch it triggers already lists it
            availableShips().single { it["id"] == blackPearlId.toString() }["name"] shouldBe "Black Pearl"
        }

        @Test
        fun `Ships sailing to another Harbor change nothing`() {
            // Given the User at "Port Royal" is looking at the fleet

            // When "Flying Dutchman" arrives at "Nassau"
            val flyingDutchman = aShippingPublishedRecord(
                shipName = "Flying Dutchman",
                catainId = seededCatainId(),
                originHarbor = "Tortuga",
                destinationHarbor = "Nassau",
            )
            kafkaTemplate.send(flyingDutchman).get()
            awaitConsumed(UUID.fromString(String(flyingDutchman.headers().lastHeader("id").value())))

            // Then the fleet of "Port Royal" is unchanged
            availableShips().shouldBeEmpty()

            // And the User is told nothing: a later Arrival on the same partition is the first thing the User is told
            val blackPearlId = UUID.randomUUID()
            kafkaTemplate.send(
                aShippingPublishedRecord(
                    shipId = blackPearlId,
                    shipName = "Black Pearl",
                    catainId = seededCatainId(),
                    destinationHarbor = "Port Royal",
                )
            ).get()
            val told = fleet.allUntil { it.name == "ship-arrived" && it.data["shipId"] == blackPearlId.toString() }
            told.none { it.data["shipName"] == "Flying Dutchman" } shouldBe true
            told.filter { it.name == "ship-arrived" }.map { it.data["shipName"] } shouldBe listOf("Black Pearl")
        }
    }

    @Nested
    @TestPropertySource(properties = ["harbor.name=Tortuga"])
    inner class AtTortuga : AHarbor() {

        @Test
        fun `A ship that arrived elsewhere leaves the fleet without a reload`() {
            // Given the User at "Tortuga" is looking at the fleet
            // And "Black Pearl" is at sea from "Tortuga" to "Port Royal"
            jdbcTemplate.givenKnownHarbors("Port Royal")
            val blackPearlId = restTemplate.aShipBeingPrepared(jdbcTemplate, "Black Pearl")
            val release = restTemplate.release(blackPearlId, "Port Royal")
            release.statusCode shouldBe HttpStatus.OK

            // When "Tortuga" learns that "Black Pearl" arrived at "Port Royal"
            kafkaTemplate.send(
                aShipArrivedRecord(
                    shipId = blackPearlId,
                    shipName = "Black Pearl",
                    shippingId = UUID.fromString(release.body!!["id"] as String),
                    originHarbor = "Tortuga",
                    destinationHarbor = "Port Royal",
                )
            ).get()

            // Then "Black Pearl" is no longer among the Available Ships of "Tortuga"
            val pushed = fleet.nextAbout("ship-left", blackPearlId)
            pushed.data["shipName"] shouldBe "Black Pearl"
            pushed.data["destinationHarbor"] shouldBe "Port Royal"
            availableShips().none { it["id"] == blackPearlId.toString() } shouldBe true
        }
    }

    /** One running Harbor with a User looking at its fleet. */
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

        @LocalServerPort
        var port: Int = 0

        lateinit var fleet: FleetEventStream

        @BeforeEach
        fun aUserIsLookingAtTheFleet() {
            jdbcTemplate.truncateMutableTables()
            jdbcTemplate.resetStockToStartingStock()
            fleet = FleetEventStream.open("http://localhost:$port")
        }

        @AfterEach
        fun theUserLooksAway() {
            fleet.close()
        }

        fun seededCatainId(): UUID =
            jdbcTemplate.queryForObject("SELECT catain_id FROM catains ORDER BY id LIMIT 1", UUID::class.java)!!

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

        /** The first pushed event named [name] about the ship [shipId]; fails if it never comes. */
        fun FleetEventStream.nextAbout(name: String, shipId: UUID): FleetEvent =
            allUntil { it.name == name && it.data["shipId"] == shipId.toString() }.last()

        /** Every pushed event up to and including the first that matches [last]; fails if it never comes. */
        fun FleetEventStream.allUntil(last: (FleetEvent) -> Boolean): List<FleetEvent> {
            val seen = mutableListOf<FleetEvent>()
            while (true) {
                val event = next().shouldNotBeNull()
                seen += event
                if (last(event)) return seen
            }
        }
    }
}
