package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.exception.HireRefusedException
import com.sonicdevelopment.domain.exception.SavingsDoNotCoverException
import com.sonicdevelopment.domain.model.Recruit
import com.sonicdevelopment.domain.model.values.CrewMemberId
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.RecruitId
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.ports.driven.RecruitPoolRepositoryPort
import com.sonicdevelopment.domain.ports.driven.SavingsRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort
import com.sonicdevelopment.domain.ports.driving.harbor.RecruitDTO
import com.sonicdevelopment.domain.ports.driving.ship.CrewManagementPort
import com.sonicdevelopment.domain.ports.driving.ship.CrewMemberDTO
import com.sonicdevelopment.domain.ports.driving.ship.HireDTO
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

/**
 * Hires a Recruit onto a ship, in one transaction. The order matters and must stay: the ship is read under its row
 * lock ([ShipRepositoryPort.lockShip]) so that Hires onto one ship run one after the other and the Crew Capacity
 * holds; the guards of [com.sonicdevelopment.domain.model.Ship.hire] run before any write; the Hiring Fee is paid by
 * the conditional debit of the Savings before anything else is written, so nothing is Hired unpaid; the Recruit is
 * taken by a conditional step, and a Recruit taken meanwhile throws so the debit rolls back; then the Crew Member is
 * added and a new Recruit is offered at the next never-used ordinal. Nothing is published.
 *
 * A Dismiss takes the same ship lock, so it and a Hire on one ship run one after the other; the guards of
 * [com.sonicdevelopment.domain.model.Ship.dismiss] run before the Crew Member is removed. It moves no money and
 * writes no Recruit Pool row: the Recruit stays taken and is never offered again. Nothing is published.
 */
@Service
class CrewManagementService(
    private val currentHarbor: HarborName,
    private val shipRepositoryPort: ShipRepositoryPort,
    private val savingsRepositoryPort: SavingsRepositoryPort,
    private val recruitPoolRepositoryPort: RecruitPoolRepositoryPort,
    private val recruitPoolGenerator: RecruitPoolGenerator,
) : CrewManagementPort {

    @Transactional
    override fun hire(shipId: ShipId, recruitId: RecruitId): HireDTO? {
        val ship = shipRepositoryPort.lockShip(shipId) ?: return null
        val recruit = recruitPoolRepositoryPort.getRecruits().firstOrNull { it.id == recruitId }
            ?: throw HireRefusedException("The Recruit is not offered in the Recruit Pool")
        val crewMember = ship.hire(recruit, currentHarbor)
        if (!savingsRepositoryPort.pay(HiringFee.AMOUNT)) {
            throw SavingsDoNotCoverException(
                HiringFee.AMOUNT, "The Savings do not cover the Hiring Fee of ${HiringFee.AMOUNT}"
            )
        }
        if (!recruitPoolRepositoryPort.takeRecruit(recruit.id)) {
            throw HireRefusedException("The Recruit is no longer offered in the Recruit Pool")
        }
        shipRepositoryPort.addCrewMember(ship.id, crewMember)
        val newRecruit = offerNewRecruit()
        return HireDTO(
            shipId = ship.id,
            shipName = ship.shipName,
            crewMember = CrewMemberDTO(crewMember.id.id, crewMember.name, crewMember.trait),
            hiringFee = HiringFee.AMOUNT,
            savings = savingsRepositoryPort.getSavings(),
            newRecruit = RecruitDTO(newRecruit.id.id, newRecruit.name, newRecruit.trait, HiringFee.AMOUNT),
        )
    }

    @Transactional
    override fun dismiss(shipId: ShipId, crewMemberId: CrewMemberId): Boolean {
        val ship = shipRepositoryPort.lockShip(shipId) ?: return false
        ship.dismiss(crewMemberId, currentHarbor)
        shipRepositoryPort.removeCrewMember(ship.id, crewMemberId)
        return true
    }

    /** A conflict means a concurrent Hire or opening took that ordinal first, so the next one is tried. */
    private fun offerNewRecruit(): Recruit {
        repeat(MAX_ATTEMPTS) {
            val ordinal = recruitPoolRepositoryPort.nextOrdinal()
            val recruit = recruitPoolGenerator.recruit(ordinal)
            if (recruitPoolRepositoryPort.rememberRecruit(ordinal, recruit)) return recruit
        }
        error("No free ordinal for a new Recruit after $MAX_ATTEMPTS attempts")
    }

    private companion object {
        const val MAX_ATTEMPTS = 10
    }
}
