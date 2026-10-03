package com.sonicdevelopment.driven.adapter.persistence.harbor

import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.ports.driven.KnownHarborRepositoryPort

class KnownHarborRepositoryAdapter : KnownHarborRepositoryPort {
    override fun rememberHarbor(harborName: HarborName): Unit = TODO()

    override fun getKnownHarbors(): List<HarborName> = TODO()
}
