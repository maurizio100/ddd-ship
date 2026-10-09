package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.domain.ports.driven.KnownHarborRepositoryPort
import com.sonicdevelopment.domain.ports.driven.SavingsRepositoryPort
import com.sonicdevelopment.domain.ports.driving.harbor.HarborInformationPort
import com.sonicdevelopment.domain.ports.driving.harbor.KnownHarborsDTO
import org.springframework.stereotype.Service

@Service
class HarborInformationService(
    private val currentHarbor: HarborName,
    private val knownHarborRepositoryPort: KnownHarborRepositoryPort,
    private val savingsRepositoryPort: SavingsRepositoryPort
) : HarborInformationPort {
    override fun getKnownHarbors(): KnownHarborsDTO =
        KnownHarborsDTO(
            harborName = currentHarbor,
            knownHarbors = knownHarborRepositoryPort.getKnownHarbors()
        )

    override fun getSavings(): Money = savingsRepositoryPort.getSavings()
}
