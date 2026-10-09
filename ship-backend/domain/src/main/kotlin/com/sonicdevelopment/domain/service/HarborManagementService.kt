package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.model.values.EventId
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.ports.driven.CargoQueryPort
import com.sonicdevelopment.domain.ports.driven.HarborOutboxRepositoryPort
import com.sonicdevelopment.domain.ports.driven.InboxRepositoryPort
import com.sonicdevelopment.domain.ports.driven.KnownHarborRepositoryPort
import com.sonicdevelopment.domain.ports.driven.PriceRepositoryPort
import com.sonicdevelopment.domain.ports.driving.harbor.HarborManagementPort
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class HarborManagementService(
    private val currentHarbor: HarborName,
    private val knownHarborRepositoryPort: KnownHarborRepositoryPort,
    private val harborOutboxRepositoryPort: HarborOutboxRepositoryPort,
    private val inboxRepositoryPort: InboxRepositoryPort,
    private val cargoQueryPort: CargoQueryPort,
    private val priceRepositoryPort: PriceRepositoryPort,
    private val priceRoll: PriceRoll
) : HarborManagementPort {

    @Transactional
    override fun openHarbor() {
        harborOutboxRepositoryPort.publishHarborOpened(currentHarbor)
    }

    /**
     * A Harbor never knows itself, and knows every other Harbor once: a re-opening Harbor sends a new
     * event, so the inbox alone cannot dedupe it; [KnownHarborRepositoryPort] keeps each name once.
     */
    @Transactional
    override fun learnAboutHarbor(eventId: EventId, harborName: HarborName) {
        if (!inboxRepositoryPort.recordConsumedEvent(eventId)) return
        if (harborName == currentHarbor) return

        knownHarborRepositoryPort.rememberHarbor(harborName)
    }
}
