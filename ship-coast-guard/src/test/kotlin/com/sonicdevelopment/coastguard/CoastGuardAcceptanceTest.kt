package com.sonicdevelopment.coastguard

import com.fasterxml.jackson.databind.ObjectMapper
import com.sonicdevelopment.coastguard.events.SHIP_IMPRISONED
import com.sonicdevelopment.coastguard.events.eventIdOf
import org.apache.kafka.common.header.internals.RecordHeader
import org.apache.kafka.common.header.internals.RecordHeaders
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.streams.TestInputTopic
import org.apache.kafka.streams.TestOutputTopic
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.test.TestRecord
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.Duration
import java.time.Instant
import java.util.Properties
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoastGuardAcceptanceTest {

    private val mapper = ObjectMapper()
    private val now = Instant.parse("2026-11-19T12:00:00Z")
    private val saltyWhisker = UUID.randomUUID()
    private val seaBiscuit = UUID.randomUUID()

    private lateinit var driver: TopologyTestDriver
    private lateinit var input: TestInputTopic<String, String>
    private lateinit var output: TestOutputTopic<String, String>

    @BeforeEach
    fun setUp() {
        val props = Properties().apply {
            put("application.id", "ship-coast-guard-test")
            put("bootstrap.servers", "dummy:9092")
        }
        driver = TopologyTestDriver(CoastGuardTopology.build(), props)
        input = driver.createInputTopic(SHIPPING_TOPIC, StringSerializer(), StringSerializer())
        output = driver.createOutputTopic(COAST_GUARD_TOPIC, StringDeserializer(), StringDeserializer())
    }

    @AfterEach
    fun tearDown() = driver.close()

    @ParameterizedTest
    @ValueSource(strings = ["Ale", "Rum", "Wine", "Tobacco"])
    fun `the third voyage carrying Contraband within 10 minutes imprisons the ship`(cargo: String) {
        release(saltyWhisker, "Salty Whisker", listOf("Rum"), minutesAgo = 5)
        release(saltyWhisker, "Salty Whisker", listOf("Rum"), minutesAgo = 3)
        val tipping = release(saltyWhisker, "Salty Whisker", listOf(cargo), minutesAgo = 0,
            origin = "Isla de Muerta", destination = "Port Royal")

        val records = output.readRecordsToList()
        assertEquals(1, records.size)
        val record = records.single()
        assertEquals(saltyWhisker.toString(), record.key)
        val payload = mapper.readTree(mapper.readValue(record.value, String::class.java))
        assertEquals(saltyWhisker.toString(), payload["shipId"].asText())
        assertEquals("Salty Whisker", payload["shipName"].asText())
        assertEquals("Port Royal", payload["holdingHarbor"].asText())
        assertEquals("Tortuga", payload["homeHarbor"].asText())
        assertEquals(tipping.toString(), payload["shippingId"].asText())
        assertEquals(eventIdOf(tipping).toString(), header(record.headers, "id"))
        assertEquals(SHIP_IMPRISONED, header(record.headers, "eventType"))
    }

    @Test
    fun `a voyage without Contraband does not count`() {
        release(saltyWhisker, "Salty Whisker", listOf("Rum"), minutesAgo = 5)
        release(saltyWhisker, "Salty Whisker", listOf("Rum"), minutesAgo = 3)
        release(saltyWhisker, "Salty Whisker", listOf("Silk", "Sugar"), minutesAgo = 0)

        assertTrue(output.isEmpty)
    }

    @Test
    fun `voyages more than 10 minutes apart do not add up`() {
        release(saltyWhisker, "Salty Whisker", listOf("Rum"), minutesAgo = 12)
        release(saltyWhisker, "Salty Whisker", listOf("Rum"), minutesAgo = 11)
        release(saltyWhisker, "Salty Whisker", listOf("Rum"), minutesAgo = 0)

        assertTrue(output.isEmpty)
    }

    @Test
    fun `a voyage counts once however often its Shipping Published arrives`() {
        release(saltyWhisker, "Salty Whisker", listOf("Rum"), minutesAgo = 5)
        val next = UUID.randomUUID()
        release(saltyWhisker, "Salty Whisker", listOf("Rum"), minutesAgo = 1, shippingId = next)
        release(saltyWhisker, "Salty Whisker", listOf("Rum"), minutesAgo = 1, shippingId = next)

        assertTrue(output.isEmpty)
    }

    @Test
    fun `the count starts again after an imprisonment`() {
        release(saltyWhisker, "Salty Whisker", listOf("Rum"), minutesAgo = 5)
        release(saltyWhisker, "Salty Whisker", listOf("Rum"), minutesAgo = 4)
        release(saltyWhisker, "Salty Whisker", listOf("Rum"), minutesAgo = 3)
        release(saltyWhisker, "Salty Whisker", listOf("Rum"), minutesAgo = 2)
        release(saltyWhisker, "Salty Whisker", listOf("Rum"), minutesAgo = 0)

        assertEquals(1, output.readRecordsToList().size)
    }

    @Test
    fun `each ship is counted on its own`() {
        release(saltyWhisker, "Salty Whisker", listOf("Rum"), minutesAgo = 4)
        release(seaBiscuit, "Sea Biscuit", listOf("Rum"), minutesAgo = 3)
        release(seaBiscuit, "Sea Biscuit", listOf("Rum"), minutesAgo = 0)

        assertTrue(output.isEmpty)
    }

    @Test
    fun `a record without the shipping-published event type is skipped`() {
        release(saltyWhisker, "Salty Whisker", listOf("Rum"), minutesAgo = 5)
        release(saltyWhisker, "Salty Whisker", listOf("Rum"), minutesAgo = 3)
        release(saltyWhisker, "Salty Whisker", listOf("Rum"), minutesAgo = 2, eventType = "ship-arrived")
        release(saltyWhisker, "Salty Whisker", listOf("Rum"), minutesAgo = 1, eventType = null)

        assertTrue(output.isEmpty)
    }

    @Test
    fun `an unknown field in Shipping Published is ignored`() {
        release(saltyWhisker, "Salty Whisker", listOf("Rum"), minutesAgo = 5)
        release(saltyWhisker, "Salty Whisker", listOf("Rum"), minutesAgo = 3)
        release(saltyWhisker, "Salty Whisker", listOf("Rum"), minutesAgo = 0, extraField = true)

        assertEquals(1, output.readRecordsToList().size)
    }

    private fun header(headers: org.apache.kafka.common.header.Headers, name: String): String? =
        headers.lastHeader(name)?.value()?.toString(Charsets.UTF_8)

    /** Pipes a raw outbox-style record (double-encoded JSON, eventType header) and returns its Shipping id. */
    private fun release(
        shipId: UUID,
        shipName: String,
        cargo: List<String>,
        minutesAgo: Long,
        origin: String = "Isla de Muerta",
        destination: String = "Port Royal",
        shippingId: UUID = UUID.randomUUID(),
        eventType: String? = "shipping-published",
        extraField: Boolean = false,
    ): UUID {
        val payload = mutableMapOf<String, Any>(
            "shipEventData" to mapOf(
                "shipId" to shipId.toString(), "shipName" to shipName,
                "homeHarbor" to "Tortuga", "earnings" to "0.00",
            ),
            "shippingEventData" to mapOf(
                "shippingId" to shippingId.toString(), "weight" to 5.0, "shippingQuote" to "10.00",
                "cargo" to cargo.map { mapOf("cargoId" to UUID.randomUUID().toString(), "cargoName" to it) },
                "originHarbor" to origin, "destinationHarbor" to destination,
            ),
            "catain" to mapOf("catainId" to UUID.randomUUID().toString(), "catainName" to "Whiskers"),
        )
        if (extraField) payload["somethingNew"] = mapOf("a" to 1)
        val value = mapper.writeValueAsString(mapper.writeValueAsString(payload))
        val headers = RecordHeaders().add(RecordHeader("id", UUID.randomUUID().toString().toByteArray()))
        if (eventType != null) headers.add(RecordHeader("eventType", eventType.toByteArray()))
        input.pipeInput(TestRecord(shippingId.toString(), value, headers, now.minus(Duration.ofMinutes(minutesAgo))))
        return shippingId
    }
}
