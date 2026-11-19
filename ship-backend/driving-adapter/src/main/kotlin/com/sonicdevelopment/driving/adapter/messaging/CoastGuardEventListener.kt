package com.sonicdevelopment.driving.adapter.messaging

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

/** Hands the events on `hexagonship-coast-guard` to the Harbor: Ship Imprisoned to the imprisonment. Ignores other event types. */
@Component
class CoastGuardEventListener {

    @KafkaListener(id = COAST_GUARD_LISTENER_ID, idIsGroup = false, topics = [COAST_GUARD_TOPIC])
    fun onCoastGuardEvent(record: ConsumerRecord<String, String>) {
    }

    companion object {
        const val COAST_GUARD_LISTENER_ID = "coast-guard-events"
        const val COAST_GUARD_TOPIC = "hexagonship-coast-guard"
    }
}
