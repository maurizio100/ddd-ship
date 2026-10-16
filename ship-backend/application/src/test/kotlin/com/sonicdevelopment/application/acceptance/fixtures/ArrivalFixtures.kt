package com.sonicdevelopment.application.acceptance.fixtures

import com.fasterxml.jackson.databind.ObjectMapper
import com.sonicdevelopment.driving.adapter.messaging.ShippingEventListener
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.apache.kafka.common.header.internals.RecordHeaders
import org.apache.kafka.common.record.TimestampType
import java.util.*

/**
 * The record Debezium's outbox EventRouter emits when [originHarbor] Releases the ship [shipName] to
 * [destinationHarbor]: keyed by the Shipping id, `id` and `eventType` headers, and the payload in the
 * `ShippingEvent` JSON shape as a JSON string literal. It is handed to the listener, not sent to a broker.
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
    homeHarbor: String? = originHarbor,
    earnings: String? = null,
): ConsumerRecord<String, String> {
    val shipEventData = mutableMapOf<String, Any>("shipId" to shipId, "shipName" to shipName)
    // null builds the event of an older Harbor, which sends no Home Harbor
    homeHarbor?.let { shipEventData["homeHarbor"] = it }
    // null builds the event of an older Harbor, which sends no Earnings
    earnings?.let { shipEventData["earnings"] = it }
    val payload = ObjectMapper().writeValueAsString(
        mapOf(
            "shipEventData" to shipEventData,
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
    return aShippingTopicRecord(shippingId, eventId, "shipping-published", payload)
}

/**
 * The record Debezium's outbox EventRouter emits when [destinationHarbor] announces that the ship
 * [shipName] of the Shipping [shippingId] has arrived: keyed by the Shipping id, `id` and `eventType`
 * headers, and the payload in the `ShipArrivedEvent` JSON shape as a JSON string literal.
 */
fun aShipArrivedRecord(
    shipId: UUID = UUID.randomUUID(),
    shipName: String = "Black Pearl",
    shippingId: UUID = UUID.randomUUID(),
    originHarbor: String = "Tortuga",
    destinationHarbor: String = "Port Royal",
    eventId: UUID = UUID.randomUUID(),
): ConsumerRecord<String, String> {
    val payload = ObjectMapper().writeValueAsString(
        mapOf(
            "shipId" to shipId,
            "shipName" to shipName,
            "shippingId" to shippingId,
            "originHarbor" to originHarbor,
            "destinationHarbor" to destinationHarbor,
        )
    )
    return aShippingTopicRecord(shippingId, eventId, "ship-arrived", payload)
}

private fun aShippingTopicRecord(
    shippingId: UUID,
    eventId: UUID,
    eventType: String,
    payload: String,
): ConsumerRecord<String, String> {
    val headers = RecordHeaders()
    headers.add(RecordHeader("id", eventId.toString().toByteArray(Charsets.UTF_8)))
    headers.add(RecordHeader("eventType", eventType.toByteArray(Charsets.UTF_8)))
    return ConsumerRecord(
        ShippingEventListener.SHIPPING_TOPIC, 0, 0L, 0L, TimestampType.CREATE_TIME,
        0, 0, shippingId.toString(), ObjectMapper().writeValueAsString(payload), headers, Optional.empty()
    )
}
