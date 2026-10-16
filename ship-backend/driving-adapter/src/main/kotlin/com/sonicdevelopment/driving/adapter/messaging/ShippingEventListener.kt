package com.sonicdevelopment.driving.adapter.messaging

import com.sonicdevelopment.domain.model.values.CargoId
import com.sonicdevelopment.domain.model.values.CatainId
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.model.values.ShippingId
import com.sonicdevelopment.domain.ports.driving.shipping.ArrivalManagementPort
import com.sonicdevelopment.domain.ports.driving.shipping.ShipArrivedDTO
import com.sonicdevelopment.domain.ports.driving.shipping.ShippingPublishedDTO
import com.sonicdevelopment.driving.adapter.messaging.events.ShipArrivedInboundEvent
import com.sonicdevelopment.driving.adapter.messaging.events.ShippingPublishedInboundEvent
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

/**
 * Hands the events on `hexagonship-shipping` to the Arrival: Shipping Published to its Destination side,
 * Ship Arrived to its Origin side. Ignores other event types.
 */
@Component
class ShippingEventListener(
    private val inboundMessageReader: InboundMessageReader,
    private val arrivalManagementPort: ArrivalManagementPort
) {

    // idIsGroup = false keeps the Harbor's consumer group (see HarborEventListener).
    @KafkaListener(id = SHIPPING_LISTENER_ID, idIsGroup = false, topics = [SHIPPING_TOPIC])
    fun onShippingEvent(record: ConsumerRecord<String, String>) {
        val message = inboundMessageReader.read(record)
        when (message.eventType) {
            SHIPPING_PUBLISHED -> {
                val event = inboundMessageReader.payloadAs(message, ShippingPublishedInboundEvent::class)
                arrivalManagementPort.receiveShippingPublished(message.eventId, toShippingPublishedDTO(event))
            }
            SHIP_ARRIVED -> {
                val event = inboundMessageReader.payloadAs(message, ShipArrivedInboundEvent::class)
                arrivalManagementPort.receiveShipArrived(message.eventId, toShipArrivedDTO(event))
            }
            else -> return
        }
    }

    private fun toShippingPublishedDTO(event: ShippingPublishedInboundEvent) =
        ShippingPublishedDTO(
            shipId = ShipId(event.shipEventData.shipId),
            shipName = event.shipEventData.shipName,
            catainId = CatainId(event.catain.catainId),
            shippingId = ShippingId(event.shippingEventData.shippingId),
            cargoIds = event.shippingEventData.cargo.map { CargoId(it.cargoId) },
            originHarbor = toHarborName(event.shippingEventData.originHarbor),
            destinationHarbor = toHarborName(event.shippingEventData.destinationHarbor),
            homeHarbor = toHarborName(event.shipEventData.homeHarbor),
            earnings = event.shipEventData.earnings?.takeIf { it.isNotBlank() }?.let { Money.of(it) } ?: Money.dollars(0),
        )

    private fun toShipArrivedDTO(event: ShipArrivedInboundEvent) =
        ShipArrivedDTO(
            shipId = ShipId(event.shipId),
            shippingId = ShippingId(event.shippingId),
            originHarbor = toHarborName(event.originHarbor),
            destinationHarbor = toHarborName(event.destinationHarbor),
        )

    private fun toHarborName(name: String?) = name?.takeIf { it.isNotBlank() }?.let { HarborName(it) }

    companion object {
        const val SHIPPING_LISTENER_ID = "shipping-events"
        const val SHIPPING_TOPIC = "hexagonship-shipping"
        private const val SHIPPING_PUBLISHED = "shipping-published"
        private const val SHIP_ARRIVED = "ship-arrived"
    }
}
