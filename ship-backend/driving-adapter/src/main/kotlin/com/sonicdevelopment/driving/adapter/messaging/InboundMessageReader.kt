package com.sonicdevelopment.driving.adapter.messaging

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.sonicdevelopment.domain.model.values.EventId
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.stereotype.Component
import java.util.*
import kotlin.reflect.KClass

/**
 * Turns a Kafka record routed by the Debezium outbox EventRouter into an [InboundMessage], and maps its
 * payload onto an inbound event copy (`events/<Name>InboundEvent`). Inbound copies tolerate unknown fields.
 */
@Component
class InboundMessageReader(objectMapper: ObjectMapper) {

    private val objectMapper: ObjectMapper = objectMapper.copy()
        .registerModule(KotlinModule.Builder().build())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    fun read(record: ConsumerRecord<String, String>): InboundMessage =
        InboundMessage(
            eventId = EventId(UUID.fromString(record.requiredHeader(EVENT_ID_HEADER))),
            eventType = record.requiredHeader(EVENT_TYPE_HEADER),
            // The outbox payload column is JSON text, so the EventRouter emits it as a JSON string literal.
            payload = objectMapper.readValue(record.value(), String::class.java)
        )

    fun <T : Any> payloadAs(message: InboundMessage, type: KClass<T>): T =
        objectMapper.readValue(message.payload, type.java)

    private fun ConsumerRecord<String, String>.requiredHeader(name: String): String {
        val header = requireNotNull(headers().lastHeader(name)) {
            "Record from ${topic()} at offset ${offset()} has no '$name' header"
        }
        return String(header.value(), Charsets.UTF_8)
    }

    private companion object {
        /** Debezium EventRouter: the outbox `message_id`. */
        const val EVENT_ID_HEADER = "id"

        /** Outbox `event_type`, placed as a header by the connector's `additional.placement`. */
        const val EVENT_TYPE_HEADER = "eventType"
    }
}
