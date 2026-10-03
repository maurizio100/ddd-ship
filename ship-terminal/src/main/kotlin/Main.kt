package org.example

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.Deserializer
import java.io.Serializable
import java.util.*
import com.fasterxml.jackson.databind.DeserializationFeature
import org.apache.kafka.common.serialization.StringDeserializer

const val SHIPPING_PUBLISHED = "shipping-published"

data class ShippingEvent @JsonCreator constructor(
    @JsonProperty("shipEventData") val shipEventData: ShipEventData,
    @JsonProperty("shippingEventData") val shippingEventData: ShippingEventData,
    @JsonProperty("catain") val catain: CatainEventData
) : Serializable {

    data class ShipEventData @JsonCreator constructor(
        @JsonProperty("shipId") val shipId: UUID,
        @JsonProperty("shipName") val shipName: String
    ) : Serializable

    data class CatainEventData @JsonCreator constructor(
        @JsonProperty("catainId") val catainId: UUID,
        @JsonProperty("catainName") val catainName: String
    ) : Serializable

    data class ShippingEventData @JsonCreator constructor(
        @JsonProperty("shippingId") val shippingId: UUID,
        @JsonProperty("weight") val weight: Float,
        @JsonProperty("shippingQuote") val shippingQuote: String?,
        @JsonProperty("cargo") val cargo: List<CargoEventData>
    ) : Serializable {
        data class CargoEventData @JsonCreator constructor(
            @JsonProperty("cargoId") val cargoId: UUID,
            @JsonProperty("cargoName") val cargoName: String
        ) : Serializable
    }
}

class ShippingEventDeserializer : Deserializer<ShippingEvent> {

    private val objectMapper = ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    override fun deserialize(topic: String?, data: ByteArray?): ShippingEvent? {
        return try {
            val mapped = objectMapper.readValue(data, String::class.java)
            return objectMapper.readValue(mapped, ShippingEvent::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

fun eventTypeOf(record: ConsumerRecord<*, *>): String? {
    val header = record.headers().lastHeader("eventType")
    return if (header != null) {
        String(header.value(), Charsets.UTF_8)
    } else {
        null
    }
}

fun shouldAnnounce(eventType: String?, event: ShippingEvent?): Boolean {
    return eventType == SHIPPING_PUBLISHED && event != null
}

fun handleRecord(record: ConsumerRecord<String, ShippingEvent?>): Boolean {
    val eventType = eventTypeOf(record)
    val event = record.value()

    return if (shouldAnnounce(eventType, event)) {
        printShippingEvent(event!!)
        true
    } else {
        val logger = System.getLogger("ship-terminal")
        logger.log(System.Logger.Level.DEBUG, "Skipping event type '$eventType'")
        false
    }
}

fun main() {
    val config = Properties()
    config["bootstrap.servers"] = "localhost:9094"
    config["group.id"] = "ship-terminal"
    config["key.deserializer"] = StringDeserializer::class.java.name
    config["value.deserializer"] = ShippingEventDeserializer::class.java.name
    config["auto.offset.reset"] = "earliest"

    val consumer = KafkaConsumer<String, ShippingEvent?>(config)
    consumer.subscribe(listOf("hexagonship-shipping"))

    println("Started consuming messages...")

    while (true) {
        val records = consumer.poll(100)
        for (record in records) {
            handleRecord(record)
        }
    }
}

fun printShippingEvent(event: ShippingEvent) {
    println("🚢 Ship Name: ${event.shipEventData.shipName}")
    println("🧑‍✈️ Captain Name: ${event.catain.catainName}")
    println("⚖️ Weight: ${event.shippingEventData.weight} kg")

    if (event.shippingEventData.cargo.isNotEmpty()) {
        println("📦 Cargo:")
        event.shippingEventData.cargo.forEachIndexed { index, cargo ->
            println("  ${index + 1}. ${cargo.cargoName}")
        }
    } else {
        println("📦 No cargo loaded.")
    }
    println("===================================")
}