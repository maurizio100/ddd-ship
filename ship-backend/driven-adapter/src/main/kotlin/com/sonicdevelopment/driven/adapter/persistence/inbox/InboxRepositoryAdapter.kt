package com.sonicdevelopment.driven.adapter.persistence.inbox

import com.sonicdevelopment.domain.model.values.EventId
import com.sonicdevelopment.domain.ports.driven.InboxRepositoryPort
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class InboxRepositoryAdapter(
    private val inboxEventPersistenceEntityRepository: InboxEventPersistenceEntityRepository
) : InboxRepositoryPort {

    /** One statement is both the check and the record, so concurrent redeliveries cannot both win. */
    @Transactional(propagation = Propagation.MANDATORY)
    override fun recordConsumedEvent(eventId: EventId): Boolean =
        inboxEventPersistenceEntityRepository.insertIfAbsent(eventId.id) == 1
}
