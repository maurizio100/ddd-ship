package com.sonicdevelopment.coastguard

import com.sonicdevelopment.coastguard.events.SHIPPING_PUBLISHED
import com.sonicdevelopment.coastguard.events.SHIP_IMPRISONED
import com.sonicdevelopment.coastguard.events.ShipImprisonedEvent
import com.sonicdevelopment.coastguard.events.ShippingPublishedEvent
import com.sonicdevelopment.coastguard.events.eventIdOf
import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.header.internals.RecordHeader
import org.apache.kafka.common.header.internals.RecordHeaders
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Produced
import org.apache.kafka.streams.kstream.Repartitioned
import org.apache.kafka.streams.processor.api.Processor
import org.apache.kafka.streams.processor.api.ProcessorContext
import org.apache.kafka.streams.processor.api.ProcessorSupplier
import org.apache.kafka.streams.processor.api.Record
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.state.Stores
import org.slf4j.LoggerFactory

const val SHIPPING_TOPIC = "hexagonship-shipping"
const val COAST_GUARD_TOPIC = "hexagonship-coast-guard"
const val SHIP_WATCHES_STORE = "ship-watches"

object CoastGuardTopology {
    fun build(): Topology {
        val builder = StreamsBuilder()
        builder.addStateStore(
            Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(SHIP_WATCHES_STORE), Serdes.String(), JsonSerdes.shipWatch,
            ),
        )
        builder.stream(SHIPPING_TOPIC, Consumed.with(Serdes.String(), Serdes.ByteArray()))
            .process(ProcessorSupplier { ContrabandVoyages() })
            .repartition(Repartitioned.with(Serdes.String(), JsonSerdes.shippingPublished).withName("ships"))
            .process(ProcessorSupplier { ShipWatchKeeper() }, SHIP_WATCHES_STORE)
            .to(COAST_GUARD_TOPIC, Produced.with(Serdes.String(), Serdes.String()))
        return builder.build()
    }
}

/** Keeps the Shipping Published voyages carrying Contraband, re-keyed by Ship Id. */
private class ContrabandVoyages : Processor<String, ByteArray, String, ShippingPublishedEvent> {
    private lateinit var context: ProcessorContext<String, ShippingPublishedEvent>

    override fun init(context: ProcessorContext<String, ShippingPublishedEvent>) {
        this.context = context
    }

    override fun process(record: Record<String, ByteArray>) {
        if (headerOf(record.headers(), "eventType") != SHIPPING_PUBLISHED) return
        val event = JsonSerdes.readShippingPublished(record.value())
        if (event == null) {
            logger.warn("Skipping unreadable shipping-published with key {}", record.key())
            return
        }
        if (!Contraband.carriesContraband(event.shippingEventData.cargo.map { it.cargoName })) return
        context.forward(record.withKey(event.shipEventData.shipId.toString()).withValue(event))
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ContrabandVoyages::class.java)
    }
}

/** Applies each voyage to its ship's [ShipWatch] and publishes Ship Imprisoned when it tips. */
private class ShipWatchKeeper : Processor<String, ShippingPublishedEvent, String, String> {
    private lateinit var context: ProcessorContext<String, String>
    private lateinit var store: KeyValueStore<String, ShipWatch>

    override fun init(context: ProcessorContext<String, String>) {
        this.context = context
        this.store = context.getStateStore(SHIP_WATCHES_STORE)
    }

    override fun process(record: Record<String, ShippingPublishedEvent>) {
        val event = record.value()
        val shippingId = event.shippingEventData.shippingId
        val (watch, imprisoned) = (store.get(record.key()) ?: ShipWatch()).record(shippingId, record.timestamp())
        store.put(record.key(), watch)
        if (!imprisoned) return

        val imprisonment = ShipImprisonedEvent(
            shipId = event.shipEventData.shipId,
            shipName = event.shipEventData.shipName,
            holdingHarbor = event.shippingEventData.destinationHarbor,
            homeHarbor = event.shipEventData.homeHarbor,
            shippingId = shippingId,
        )
        val headers = RecordHeaders()
            .add(RecordHeader("id", eventIdOf(shippingId).toString().toByteArray()))
            .add(RecordHeader("eventType", SHIP_IMPRISONED.toByteArray()))
        context.forward(record.withValue(JsonSerdes.writeShipImprisoned(imprisonment)).withHeaders(headers))
    }
}

private fun headerOf(headers: Headers, name: String): String? =
    headers.lastHeader(name)?.value()?.toString(Charsets.UTF_8)
