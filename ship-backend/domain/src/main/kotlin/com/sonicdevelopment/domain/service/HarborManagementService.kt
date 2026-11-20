package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.model.values.EventId
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.ports.driven.CargoQueryPort
import com.sonicdevelopment.domain.ports.driven.HarborOutboxRepositoryPort
import com.sonicdevelopment.domain.ports.driven.InboxRepositoryPort
import com.sonicdevelopment.domain.ports.driven.KnownHarborRepositoryPort
import com.sonicdevelopment.domain.ports.driven.PriceRepositoryPort
import com.sonicdevelopment.domain.ports.driven.RecruitPoolRepositoryPort
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
    private val priceRoll: PriceRoll,
    private val recruitPoolRepositoryPort: RecruitPoolRepositoryPort,
    private val recruitPoolGenerator: RecruitPoolGenerator
) : HarborManagementPort {

    /**
     * Rolls a Price for every catalog Cargo that has none yet, tops the Recruit Pool up to [RECRUIT_POOL_SIZE],
     * then publishes Harbor Opened, in one transaction. A Price or Recruit once stored is kept, so opening
     * again changes neither.
     */
    @Transactional
    override fun openHarbor() {
        val prices = priceRepositoryPort.getPrices()
        cargoQueryPort.findAllCargo()
            .filterNot { it.id in prices }
            .forEach { priceRepositoryPort.rememberPrice(it.id, priceRoll.roll()) }
        val missing = RECRUIT_POOL_SIZE - recruitPoolRepositoryPort.getRecruits().size
        if (missing > 0) {
            val first = recruitPoolRepositoryPort.nextOrdinal()
            (first until first + missing).forEach {
                recruitPoolRepositoryPort.rememberRecruit(it, recruitPoolGenerator.recruit(it))
            }
        }
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
