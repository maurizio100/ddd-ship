package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.model.values.EventId
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.ports.driven.HarborOutboxRepositoryPort
import com.sonicdevelopment.domain.ports.driven.InboxRepositoryPort
import com.sonicdevelopment.domain.ports.driven.KnownHarborRepositoryPort
import com.sonicdevelopment.domain.ports.driving.harbor.HarborManagementPort

class HarborManagementService(
    private val currentHarbor: HarborName,
    private val knownHarborRepositoryPort: KnownHarborRepositoryPort,
    private val harborOutboxRepositoryPort: HarborOutboxRepositoryPort,
    private val inboxRepositoryPort: InboxRepositoryPort
) : HarborManagementPort {
    override fun openHarbor(): Unit = TODO()

    override fun learnAboutHarbor(eventId: EventId, harborName: HarborName): Unit = TODO()
}
