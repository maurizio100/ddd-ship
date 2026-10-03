package com.sonicdevelopment.application.acceptance.fixtures

import com.fasterxml.jackson.databind.ObjectMapper
import com.sonicdevelopment.application.KafkaTestcontainer
import org.apache.kafka.clients.producer.ProducerRecord
import java.util.*

/**
 * The record Debezium's outbox EventRouter emits when [originHarbor] Releases the ship [shipName] to
 * [destinationHarbor]: keyed by the Shipping id, `id` and `eventType` headers, and the payload in the
 * `ShippingEvent` JSON shape as a JSON string literal.
 */
fun aShippingPublishedRecord(
    shipId: UUID = UUID.randomUUID(),
    shipName: String = "Black Pearl",
    catainId: UUID = UUID.randomUUID(),
    shippingId: UUID = UUID.randomUUID(),
    cargoIds: List<UUID> = emptyList(),
    originHarbor: String = "Tortuga",
    destinationHarbor: String = "Port Royal",
    eventId: UUID = UUID.randomUUID(),
): ProducerRecord<String, String> {
    val payload = ObjectMapper().writeValueAsString(
        mapOf(
            "shipEventData" to mapOf("shipId" to shipId, "shipName" to shipName),
            "shippingEventData" to mapOf(
                "shippingId" to shippingId,
                "weight" to 7.5,
                "shippingQuote" to "Fair winds",
                "cargo" to cargoIds.map { mapOf("cargoId" to it, "cargoName" to "Cargo") },
                "originHarbor" to originHarbor,
                "destinationHarbor" to destinationHarbor,
            ),
            "catain" to mapOf("catainId" to catainId, "catainName" to "Catain"),
        )
    )
    return ProducerRecord<String, String>(
        KafkaTestcontainer.SHIPPING_TOPIC,
        null,
        shippingId.toString(),
        ObjectMapper().writeValueAsString(payload),
    ).apply {
        headers().add("id", eventId.toString().toByteArray(Charsets.UTF_8))
        headers().add("eventType", "shipping-published".toByteArray(Charsets.UTF_8))
    }
}
