package com.sonicdevelopment.domain.ports.driven

import com.sonicdevelopment.domain.model.values.HarborName

/** The Known Harbors of this Harbor: every other Harbor it has learned of, each at most once. */
interface KnownHarborRepositoryPort {
    /**
     * Adds [harborName] to the Known Harbors unless it is already one of them.
     *
     * Must run inside the caller's transaction, so it commits together with the inbox record of the
     * event that taught it.
     */
    fun rememberHarbor(harborName: HarborName)

    /** The Known Harbors, ordered by Harbor Name. */
    fun getKnownHarbors(): List<HarborName>
}
