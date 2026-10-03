package com.sonicdevelopment.driven.adapter.persistence.outbox

import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.ports.driven.HarborOutboxRepositoryPort

class HarborOutboxRepositoryAdapter : HarborOutboxRepositoryPort {
    override fun publishHarborOpened(harborName: HarborName): Unit = TODO()
}
