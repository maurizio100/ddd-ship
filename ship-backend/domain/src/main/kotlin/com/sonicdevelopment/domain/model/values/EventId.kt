package com.sonicdevelopment.domain.model.values

import java.util.*

/** Identity of an event consumed from another Harbor: the outbox `message_id` of the publishing Harbor. */
data class EventId(val id: UUID)
