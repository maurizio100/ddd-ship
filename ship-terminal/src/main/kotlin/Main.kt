package org.example

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
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
        @JsonProperty("shipName") val shipName: String,
        @JsonProperty("crew") val crew: List<CrewEventData>?
    ) : Serializable

    data class CrewEventData @JsonCreator constructor(
        @JsonProperty("crewMemberId") val crewMemberId: UUID,
        @JsonProperty("name") val name: String,
        @JsonProperty("trait") val trait: String
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

data class ShipArrivedEvent @JsonCreator constructor(
    @JsonProperty("shipId") val shipId: UUID,
    @JsonProperty("shipName") val shipName: String,
    @JsonProperty("shippingId") val shippingId: UUID?,
    @JsonProperty("originHarbor") val originHarbor: String,
    @JsonProperty("destinationHarbor") val destinationHarbor: String
) : Serializable

data class HarborOpenedEvent @JsonCreator constructor(
    @JsonProperty("harborName") val harborName: String
) : Serializable

data class ShipImprisonedEvent @JsonCreator constructor(
    @JsonProperty("shipId") val shipId: UUID,
    @JsonProperty("shipName") val shipName: String,
    @JsonProperty("holdingHarbor") val holdingHarbor: String,
    @JsonProperty("homeHarbor") val homeHarbor: String,
    @JsonProperty("shippingId") val shippingId: UUID?
) : Serializable

private val objectMapper = ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

/** Reads a JSON string literal (outbox, Coast Guard) or a plain JSON object; null when unreadable. */
fun <T> parseEnvelope(raw: String?, type: Class<T>): T? {
    if (raw == null) return null
    return try {
        val node = objectMapper.readTree(raw)
        if (node.isTextual) objectMapper.readValue(node.asText(), type) else objectMapper.treeToValue(node, type)
    } catch (e: Exception) {
        null
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

private val logger = System.getLogger("ship-terminal")

private fun <T> announce(raw: String?, type: Class<T>, kind: String, out: (String) -> Unit, format: (T) -> String): Boolean {
    val event = parseEnvelope(raw, type)
    if (event == null) {
        out("Could not read a $kind")
        return false
    }
    out(format(event))
    return true
}

fun handleRecord(record: ConsumerRecord<String, String?>, out: (String) -> Unit = ::println): Boolean {
    val kind = eventTypeOf(record)
    val raw = record.value()
    return when (kind) {
        SHIPPING_PUBLISHED -> announce(raw, ShippingEvent::class.java, "Shipping Published", out, ::formatDeparture)
        "ship-arrived" -> announce(raw, ShipArrivedEvent::class.java, "Ship Arrived", out) {
            "\uD83D\uDEA2 ${it.shipName} has arrived at ${it.destinationHarbor} from ${it.originHarbor}"
        }
        "harbor-opened" -> announce(raw, HarborOpenedEvent::class.java, "Harbor Opened", out) {
            "\u2693 Harbor ${it.harborName} has opened"
        }
        "ship-imprisoned" -> announce(raw, ShipImprisonedEvent::class.java, "Ship Imprisoned", out) {
            "\u26D3 ${it.shipName} is imprisoned at ${it.holdingHarbor} and its Home Harbor is ${it.homeHarbor}"
        }
        else -> {
            out("Unknown Event $kind: ${raw?.replace("\n", " ")}")
            false
        }
    }
}

fun main() {
    val config = Properties()
    config["bootstrap.servers"] = "localhost:9094"
    config["group.id"] = "ship-terminal"
    config["key.deserializer"] = StringDeserializer::class.java.name
    config["value.deserializer"] = StringDeserializer::class.java.name
    config["auto.offset.reset"] = "earliest"

    val consumer = KafkaConsumer<String, String?>(config)
    consumer.subscribe(listOf("hexagonship-shipping", "hexagonship-harbor", "hexagonship-coast-guard"))

    println("Started consuming messages...")

    while (true) {
        val records = consumer.poll(100)
        for (record in records) {
            try {
                handleRecord(record)
            } catch (e: Exception) {
                logger.log(System.Logger.Level.ERROR, "Failed to handle record at offset ${record.offset()}: ${e.message}")
            }
        }
    }
}

fun formatDeparture(event: ShippingEvent): String {
    val lines = mutableListOf(
        "🚢 Ship Name: ${event.shipEventData.shipName}",
        "🧑‍✈️ Captain Name: ${event.catain.catainName}",
        "⚖️ Weight: ${event.shippingEventData.weight} kg"
    )
    if (event.shippingEventData.cargo.isNotEmpty()) {
        lines += "📦 Cargo:"
        event.shippingEventData.cargo.forEachIndexed { index, cargo -> lines += "  ${index + 1}. ${cargo.cargoName}" }
    } else {
        lines += "📦 No cargo loaded."
    }
    val crew = event.shipEventData.crew
    if (!crew.isNullOrEmpty()) {
        lines += "🐾 Crew:"
        crew.forEachIndexed { index, member -> lines += "  ${index + 1}. ${member.name} (${member.trait})" }
    } else {
        lines += "🐾 Sails without Crew."
    }
    lines += "==================================="
    return lines.joinToString("\n")
}
