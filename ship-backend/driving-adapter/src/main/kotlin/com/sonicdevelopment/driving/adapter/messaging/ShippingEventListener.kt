package com.sonicdevelopment.driving.adapter.messaging

import com.sonicdevelopment.domain.ports.driving.shipping.ArrivalManagementPort
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.stereotype.Component

@Component
class ShippingEventListener(
    private val inboundMessageReader: InboundMessageReader,
    private val arrivalManagementPort: ArrivalManagementPort
) {

    fun onShippingEvent(record: ConsumerRecord<String, String>) {
    }

    companion object {
        const val SHIPPING_LISTENER_ID = "shipping-events"
        const val SHIPPING_TOPIC = "hexagonship-shipping"
    }
}
