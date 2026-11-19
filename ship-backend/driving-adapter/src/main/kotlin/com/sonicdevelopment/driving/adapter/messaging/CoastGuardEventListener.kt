package com.sonicdevelopment.driving.adapter.messaging

import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.ports.driving.shipping.ImprisonmentManagementPort
import com.sonicdevelopment.domain.ports.driving.shipping.ShipImprisonedDTO
import com.sonicdevelopment.driving.adapter.messaging.events.ShipImprisonedInboundEvent
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

/**
 * Hands the events on `hexagonship-coast-guard` to the Harbor: Ship Imprisoned to the imprisonment. Ignores other
 * event types. A failure propagates, so the record is redelivered; the inbox drops it once handled.
 */
@Component
class CoastGuardEventListener(
    private val inboundMessageReader: InboundMessageReader,
    private val imprisonmentManagementPort: ImprisonmentManagementPort,
) {

    // idIsGroup = false keeps the Harbor's consumer group (see HarborEventListener).
    @KafkaListener(id = COAST_GUARD_LISTENER_ID, idIsGroup = false, topics = [COAST_GUARD_TOPIC])
    fun onCoastGuardEvent(record: ConsumerRecord<String, String>) {
        val message = inboundMessageReader.read(record)
        when (message.eventType) {
            SHIP_IMPRISONED -> {
                val event = inboundMessageReader.payloadAs(message, ShipImprisonedInboundEvent::class)
                imprisonmentManagementPort.receiveShipImprisoned(message.eventId, toShipImprisonedDTO(event))
            }
            else -> return
        }
    }

    private fun toShipImprisonedDTO(event: ShipImprisonedInboundEvent) =
        ShipImprisonedDTO(
            shipId = ShipId(event.shipId),
            shipName = event.shipName,
            holdingHarbor = HarborName(event.holdingHarbor),
            homeHarbor = HarborName(event.homeHarbor),
        )

    companion object {
        const val COAST_GUARD_LISTENER_ID = "coast-guard-events"
        const val COAST_GUARD_TOPIC = "hexagonship-coast-guard"
        private const val SHIP_IMPRISONED = "ship-imprisoned"
    }
}
