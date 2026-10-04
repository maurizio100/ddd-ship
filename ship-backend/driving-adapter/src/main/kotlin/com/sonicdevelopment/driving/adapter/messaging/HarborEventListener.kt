package com.sonicdevelopment.driving.adapter.messaging

import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.ports.driving.harbor.HarborManagementPort
import com.sonicdevelopment.driving.adapter.messaging.events.HarborOpenedInboundEvent
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

/** Learns about the other Harbors from the Harbor events on `hexagonship-harbor` (ADR-0005). */
@Component
class HarborEventListener(
    private val inboundMessageReader: InboundMessageReader,
    private val harborManagementPort: HarborManagementPort
) {

    // idIsGroup = false keeps the Harbor's consumer group (ship-backend-<Harbor Name>); with the default,
    // the listener id would replace it and every Harbor would share one group.
    @KafkaListener(id = HARBOR_LISTENER_ID, idIsGroup = false, topics = [HARBOR_TOPIC])
    fun onHarborEvent(record: ConsumerRecord<String, String>) {
        val message = inboundMessageReader.read(record)
        if (message.eventType != HARBOR_OPENED) return

        val event = inboundMessageReader.payloadAs(message, HarborOpenedInboundEvent::class)
        harborManagementPort.learnAboutHarbor(message.eventId, HarborName(event.harborName))
    }

    companion object {
        const val HARBOR_LISTENER_ID = "harbor-events"
        const val HARBOR_TOPIC = "hexagonship-harbor"
        private const val HARBOR_OPENED = "harbor-opened"
    }
}
