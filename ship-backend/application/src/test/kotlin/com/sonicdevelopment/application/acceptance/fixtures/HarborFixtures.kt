package com.sonicdevelopment.application.acceptance.fixtures

import com.fasterxml.jackson.databind.ObjectMapper
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.driving.adapter.messaging.HarborEventListener
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.apache.kafka.common.header.internals.RecordHeaders
import org.apache.kafka.common.record.TimestampType
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import java.util.*

/**
 * The record Debezium's outbox EventRouter emits when the Harbor [harborName] opens, handed to the listener: keyed by the
 * Harbor, `id` and `eventType` headers, and the JSON payload as a JSON string literal.
 */
fun aHarborOpenedRecord(
    harborName: String = "Port Royal",
    eventId: UUID = UUID.randomUUID(),
): ConsumerRecord<String, String> {
    val payload = ObjectMapper().writeValueAsString(mapOf("harborName" to harborName))
    val headers = RecordHeaders()
    headers.add(RecordHeader("id", eventId.toString().toByteArray(Charsets.UTF_8)))
    headers.add(RecordHeader("eventType", "harbor-opened".toByteArray(Charsets.UTF_8)))
    return ConsumerRecord(
        HarborEventListener.HARBOR_TOPIC, 0, 0L, 0L, TimestampType.CREATE_TIME,
        0, 0, UUID.nameUUIDFromBytes(harborName.toByteArray(Charsets.UTF_8)).toString(),
        ObjectMapper().writeValueAsString(payload), headers, Optional.empty()
    )
}

/** This Harbor knows the Harbors [names], as if it had learned of them from their Harbor Opened. */
fun FakeDrivenPorts.givenKnownHarbors(vararg names: String) {
    names.forEach { knownHarbors.rememberHarbor(HarborName(it)) }
}

/** The User Releases the ship [shipId] to [destinationHarbor]. */
fun TestRestTemplate.release(shipId: UUID, destinationHarbor: String): ResponseEntity<Map<*, *>> =
    exchange(
        "/web/ships/$shipId/shippings",
        HttpMethod.PUT,
        HttpEntity(mapOf("destinationHarbor" to destinationHarbor)),
        object : ParameterizedTypeReference<Map<*, *>>() {},
    )
