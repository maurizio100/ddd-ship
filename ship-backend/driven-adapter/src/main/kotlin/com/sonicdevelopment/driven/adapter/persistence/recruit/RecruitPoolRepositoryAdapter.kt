package com.sonicdevelopment.driven.adapter.persistence.recruit

import com.sonicdevelopment.domain.model.Recruit
import com.sonicdevelopment.domain.model.values.RecruitId
import com.sonicdevelopment.domain.ports.driven.RecruitPoolRepositoryPort
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class RecruitPoolRepositoryAdapter(
    private val recruitPersistenceEntityRepository: RecruitPersistenceEntityRepository
) : RecruitPoolRepositoryPort {

    override fun getRecruits(): List<Recruit> =
        recruitPersistenceEntityRepository.findAllByRecruitHiredFalseOrderByOrdinalAsc()
            .map { Recruit(RecruitId(it.recruitId), it.name, it.trait) }

    override fun nextOrdinal(): Int = (recruitPersistenceEntityRepository.findMaxOrdinal() ?: -1) + 1

    /** One conditional `UPDATE`: the first Hire of a Recruit wins; the row stays so its ordinal is never reused. */
    @Transactional(propagation = Propagation.MANDATORY)
    override fun takeRecruit(recruitId: RecruitId): Boolean =
        recruitPersistenceEntityRepository.hire(recruitId.id) == 1

    /** One `INSERT … ON CONFLICT DO NOTHING`, so the first Recruit per ordinal wins, also against a concurrent opening. */
    @Transactional(propagation = Propagation.MANDATORY)
    override fun rememberRecruit(ordinal: Int, recruit: Recruit): Boolean =
        recruitPersistenceEntityRepository.remember(recruit.id.id, ordinal, recruit.name, recruit.trait) == 1
}
