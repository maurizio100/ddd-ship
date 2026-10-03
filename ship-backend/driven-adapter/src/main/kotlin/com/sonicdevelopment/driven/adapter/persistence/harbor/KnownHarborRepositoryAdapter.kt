package com.sonicdevelopment.driven.adapter.persistence.harbor

import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.ports.driven.KnownHarborRepositoryPort
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class KnownHarborRepositoryAdapter(
    private val knownHarborPersistenceEntityRepository: KnownHarborPersistenceEntityRepository
) : KnownHarborRepositoryPort {

    /** One statement is both the check and the insert, so concurrent re-opens cannot both win. */
    @Transactional(propagation = Propagation.MANDATORY)
    override fun rememberHarbor(harborName: HarborName) {
        knownHarborPersistenceEntityRepository.insertIfAbsent(harborName.name)
    }

    override fun getKnownHarbors(): List<HarborName> =
        knownHarborPersistenceEntityRepository.findAllByOrderByHarborNameAsc().map { HarborName(it.harborName) }
}
