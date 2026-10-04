package com.sonicdevelopment.driving.adapter.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import com.sonicdevelopment.domain.model.values.EventId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.apache.kafka.common.header.internals.RecordHeaders
import org.apache.kafka.common.record.TimestampType
import org.junit.jupiter.api.Test
import java.util.*

class InboundMessageReaderTest {

    private val reader = InboundMessageReader(ObjectMapper())

    @Test
    fun `reads event id and event type from the Debezium headers`() {
        val eventId = UUID.randomUUID()

        val message = reader.read(aRecord(id = eventId.toString(), eventType = "shipping-published"))

        message.eventId shouldBe EventId(eventId)
        message.eventType shouldBe "shipping-published"
    }

    @Test
    fun `unwraps the double-encoded JSON payload`() {
        val innerJson = """{"shipId":"6f1c2c5e-6f0a-4d39-9a8c-1d6f0e7b4a11","weight":12.5}"""
        val doubleEncoded = ObjectMapper().writeValueAsString(innerJson)

        val message = reader.read(aRecord(value = doubleEncoded))

        message.payload shouldBe innerJson
    }

    @Test
    fun `maps a payload onto an inbound copy ignoring unknown fields`() {
        val shipId = UUID.randomUUID()
        val message = InboundMessage(
            eventId = EventId(UUID.randomUUID()),
            eventType = "shipping-published",
            payload = """{"shipId":"$shipId","weight":12.5,"addedLater":"ignored"}"""
        )

        val event = reader.payloadAs(message, ProbeInboundEvent::class)

        event shouldBe ProbeInboundEvent(shipId = shipId, weight = 12.5f)
    }

    @Test
    fun `rejects a record without an id header`() {
        shouldThrow<IllegalArgumentException> {
            reader.read(aRecord(id = null))
        }
    }

    private data class ProbeInboundEvent(val shipId: UUID, val weight: Float)

    private fun aRecord(
        id: String? = UUID.randomUUID().toString(),
        eventType: String? = "shipping-published",
        value: String = "\"{}\"",
    ): ConsumerRecord<String, String> {
        val headers = RecordHeaders()
        id?.let { headers.add(RecordHeader("id", it.toByteArray(Charsets.UTF_8))) }
        eventType?.let { headers.add(RecordHeader("eventType", it.toByteArray(Charsets.UTF_8))) }
        return ConsumerRecord(
            "hexagonship-shipping", 0, 0L, 0L, TimestampType.CREATE_TIME,
            0, 0, UUID.randomUUID().toString(), value, headers, Optional.empty()
        )
    }
}
