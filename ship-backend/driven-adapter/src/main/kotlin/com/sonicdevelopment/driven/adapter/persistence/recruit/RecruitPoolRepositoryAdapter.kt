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
        recruitPersistenceEntityRepository.findAllByOrderByOrdinalAsc()
            .map { Recruit(RecruitId(it.recruitId), it.name, it.trait) }

    override fun nextOrdinal(): Int = (recruitPersistenceEntityRepository.findMaxOrdinal() ?: -1) + 1

    /** One `INSERT … ON CONFLICT DO NOTHING`, so the first Recruit per ordinal wins, also against a concurrent opening. */
    @Transactional(propagation = Propagation.MANDATORY)
    override fun rememberRecruit(ordinal: Int, recruit: Recruit): Boolean =
        recruitPersistenceEntityRepository.remember(recruit.id.id, ordinal, recruit.name, recruit.trait) == 1
}
