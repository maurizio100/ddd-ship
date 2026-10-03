package com.sonicdevelopment.driven.adapter.persistence.inbox

import com.sonicdevelopment.domain.model.values.EventId
import com.sonicdevelopment.domain.ports.driven.InboxRepositoryPort
import org.springframework.stereotype.Component

@Component
class InboxRepositoryAdapter : InboxRepositoryPort {

    override fun recordConsumedEvent(eventId: EventId): Boolean = TODO()
}
