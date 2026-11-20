package com.sonicdevelopment.application.acceptance.fixtures

import com.sonicdevelopment.domain.model.Recruit
import com.sonicdevelopment.domain.ports.driven.RecruitPoolRepositoryPort
import java.util.TreeMap

/** The Recruit Pool in memory. Mirrors the adapter: the first write per ordinal wins. A fresh Harbor has none. */
class InMemoryRecruits : RecruitPoolRepositoryPort {

    private val recruits = TreeMap<Int, Recruit>()

    @Synchronized
    override fun getRecruits(): List<Recruit> = recruits.values.toList()

    @Synchronized
    override fun nextOrdinal(): Int = (recruits.lastEntry()?.key ?: -1) + 1

    @Synchronized
    override fun rememberRecruit(ordinal: Int, recruit: Recruit): Boolean {
        if (ordinal in recruits) return false
        recruits[ordinal] = recruit
        return true
    }

    @Synchronized
    fun reset() {
        recruits.clear()
    }
}
