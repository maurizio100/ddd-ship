package com.sonicdevelopment.application.acceptance.fixtures

import com.fasterxml.jackson.databind.ObjectMapper
import com.sonicdevelopment.application.KafkaTestcontainer
import org.apache.kafka.clients.producer.ProducerRecord
import java.util.*

/**
 * The record Debezium's outbox EventRouter emits when the Harbor [harborName] opens: keyed by the
 * Harbor, `id` and `eventType` headers, and the JSON payload as a JSON string literal.
 */
fun aHarborOpenedRecord(
    harborName: String = "Port Royal",
    eventId: UUID = UUID.randomUUID(),
): ProducerRecord<String, String> {
    val payload = ObjectMapper().writeValueAsString(mapOf("harborName" to harborName))
    return ProducerRecord<String, String>(
        KafkaTestcontainer.HARBOR_TOPIC,
        null,
        UUID.nameUUIDFromBytes(harborName.toByteArray(Charsets.UTF_8)).toString(),
        ObjectMapper().writeValueAsString(payload),
    ).apply {
        headers().add("id", eventId.toString().toByteArray(Charsets.UTF_8))
        headers().add("eventType", "harbor-opened".toByteArray(Charsets.UTF_8))
    }
}
