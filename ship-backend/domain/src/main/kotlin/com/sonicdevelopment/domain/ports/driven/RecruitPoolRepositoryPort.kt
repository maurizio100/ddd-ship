package com.sonicdevelopment.domain.ports.driven

import com.sonicdevelopment.domain.model.Recruit
import com.sonicdevelopment.domain.model.values.RecruitId

interface RecruitPoolRepositoryPort {
    /** The Recruits still offered in this Harbor's Recruit Pool (not Hired), ordered by ordinal. */
    fun getRecruits(): List<Recruit>

    /** The ordinal of the next Recruit never generated at this Harbor: the highest ordinal so far plus one, 0 for none. */
    fun nextOrdinal(): Int

    /**
     * Marks an offered Recruit hired in one conditional step; returns false, changing nothing, if it is not offered
     * (anymore). Runs in the caller's transaction.
     */
    fun takeRecruit(recruitId: RecruitId): Boolean

    /** Stores [recruit] under [ordinal]; the first write per ordinal wins. Returns false if the ordinal is taken. */
    fun rememberRecruit(ordinal: Int, recruit: Recruit): Boolean
}
