package com.sonicdevelopment.driving.adapter.messaging

import com.sonicdevelopment.domain.model.values.EventId

/** An event consumed from Kafka: its id, its `event_type` and its JSON payload, already unwrapped. */
data class InboundMessage(val eventId: EventId, val eventType: String, val payload: String)
