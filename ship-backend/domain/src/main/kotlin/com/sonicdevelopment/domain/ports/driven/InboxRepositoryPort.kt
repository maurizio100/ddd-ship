package com.sonicdevelopment.domain.ports.driven

import com.sonicdevelopment.domain.model.values.EventId

/**
 * The inbox of consumed events (ADR-0004): makes sure a redelivered event never takes effect twice.
 */
interface InboxRepositoryPort {
    /**
     * Records [eventId] as consumed.
     *
     * Must run inside the caller's transaction, so the record commits or rolls back together with the
     * state change the event causes.
     *
     * @return `true` when the event id was newly recorded, `false` when it had already been consumed.
     */
    fun recordConsumedEvent(eventId: EventId): Boolean
}
