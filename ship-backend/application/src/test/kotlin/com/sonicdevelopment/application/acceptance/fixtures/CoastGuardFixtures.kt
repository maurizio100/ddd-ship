package com.sonicdevelopment.application.acceptance.fixtures

import com.fasterxml.jackson.databind.ObjectMapper
import com.sonicdevelopment.driving.adapter.messaging.CoastGuardEventListener
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.apache.kafka.common.header.internals.RecordHeaders
import org.apache.kafka.common.record.TimestampType
import java.util.*

/**
 * The record Debezium's outbox EventRouter emits when the Coast Guard imprisons the ship [shipName]: keyed by the
 * Ship Id, `id` and `eventType` headers, and the payload in the `ShipImprisonedEvent` JSON shape as a JSON
 * string literal. It is handed to the listener, not sent to a broker.
 */
fun aShipImprisonedRecord(
    shipId: UUID = UUID.randomUUID(),
    shipName: String = "Black Pearl",
    holdingHarbor: String = "Port Royal",
    homeHarbor: String = "Tortuga",
    shippingId: UUID = UUID.randomUUID(),
    eventId: UUID = UUID.randomUUID(),
): ConsumerRecord<String, String> {
    val payload = ObjectMapper().writeValueAsString(
        mapOf(
            "shipId" to shipId,
            "shipName" to shipName,
            "holdingHarbor" to holdingHarbor,
            "homeHarbor" to homeHarbor,
            "shippingId" to shippingId,
        )
    )
    val headers = RecordHeaders()
    headers.add(RecordHeader("id", eventId.toString().toByteArray(Charsets.UTF_8)))
    headers.add(RecordHeader("eventType", "ship-imprisoned".toByteArray(Charsets.UTF_8)))
    return ConsumerRecord(
        CoastGuardEventListener.COAST_GUARD_TOPIC, 0, 0L, 0L, TimestampType.CREATE_TIME,
        0, 0, shipId.toString(), ObjectMapper().writeValueAsString(payload), headers, Optional.empty()
    )
}
