package com.sonicdevelopment.domain.ports.driven

import com.sonicdevelopment.domain.model.Recruit

interface RecruitPoolRepositoryPort {
    /** The Recruits of this Harbor's Recruit Pool, ordered by ordinal. */
    fun getRecruits(): List<Recruit>

    /** The ordinal of the next Recruit never generated at this Harbor: the highest ordinal so far plus one, 0 for none. */
    fun nextOrdinal(): Int

    /** Stores [recruit] under [ordinal]; the first write per ordinal wins. Returns false if the ordinal is taken. */
    fun rememberRecruit(ordinal: Int, recruit: Recruit): Boolean
}
