package com.sonicdevelopment.domain.ports.driven

import com.sonicdevelopment.domain.model.values.HarborName

/** Writes Harbor events to the transactional outbox (ADR-0002, ADR-0005). */
interface HarborOutboxRepositoryPort {
    /** Writes Harbor Opened for [harborName] to the outbox, in the caller's transaction. */
    fun publishHarborOpened(harborName: HarborName)
}
