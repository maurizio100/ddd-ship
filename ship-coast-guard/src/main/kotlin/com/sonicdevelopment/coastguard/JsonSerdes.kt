package com.sonicdevelopment.coastguard

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sonicdevelopment.coastguard.events.ShipImprisonedEvent
import com.sonicdevelopment.coastguard.events.ShippingPublishedEvent
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.Serializer

object JsonSerdes {
    private val mapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    val shippingPublished: Serde<ShippingPublishedEvent> = serdeOf(ShippingPublishedEvent::class.java)
    val shipWatch: Serde<ShipWatch> = serdeOf(ShipWatch::class.java)

    /** Reads an outbox value, a JSON string literal holding the payload JSON; null when unreadable. */
    fun readShippingPublished(bytes: ByteArray?): ShippingPublishedEvent? = try {
        bytes?.let { mapper.readValue(mapper.readValue(it, String::class.java), ShippingPublishedEvent::class.java) }
    } catch (e: Exception) {
        null
    }

    /** Writes the payload JSON wrapped in a JSON string literal, the envelope the Harbors' inbox reads. */
    fun writeShipImprisoned(event: ShipImprisonedEvent): String =
        mapper.writeValueAsString(mapper.writeValueAsString(event))

    private fun <T> serdeOf(type: Class<T>): Serde<T> = Serdes.serdeFrom(
        Serializer<T> { _, data -> data?.let { mapper.writeValueAsBytes(it) } },
        Deserializer<T> { _, bytes -> bytes?.let { mapper.readValue(it, type) } },
    )
}
