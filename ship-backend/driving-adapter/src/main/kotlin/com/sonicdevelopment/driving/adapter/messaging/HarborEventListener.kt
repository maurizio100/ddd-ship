package com.sonicdevelopment.driving.adapter.messaging

import com.sonicdevelopment.domain.ports.driving.harbor.HarborManagementPort
import org.apache.kafka.clients.consumer.ConsumerRecord

class HarborEventListener(
    private val inboundMessageReader: InboundMessageReader,
    private val harborManagementPort: HarborManagementPort
) {
    fun onHarborEvent(record: ConsumerRecord<String, String>): Unit = TODO()
}
