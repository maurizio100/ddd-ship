package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.ports.driven.KnownHarborRepositoryPort
import com.sonicdevelopment.domain.ports.driving.harbor.HarborInformationPort
import com.sonicdevelopment.domain.ports.driving.harbor.KnownHarborsDTO

class HarborInformationService(
    private val currentHarbor: HarborName,
    private val knownHarborRepositoryPort: KnownHarborRepositoryPort
) : HarborInformationPort {
    override fun getKnownHarbors(): KnownHarborsDTO = TODO()
}
