package com.sonicdevelopment.application.acceptance.fixtures

import com.fasterxml.jackson.databind.ObjectMapper
import com.sonicdevelopment.application.KafkaTestcontainer
import com.sonicdevelopment.domain.model.values.HarborName
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
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
