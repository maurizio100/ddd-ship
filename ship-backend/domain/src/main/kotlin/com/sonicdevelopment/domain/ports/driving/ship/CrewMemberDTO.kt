package com.sonicdevelopment.domain.ports.driving.ship

import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.domain.model.values.Portrait
import com.sonicdevelopment.domain.service.HiringFee
import java.util.UUID

/** A Crew Member aboard a ship. */
data class CrewMemberDTO(val id: UUID, val name: String, val trait: String) {
    /** The Recruit Portrait, derived from the UUID alone. */
    val portrait: Portrait get() = Portrait.of(id)

    /** What a Dismiss refunds into the Savings: half the Hiring Fee. */
    val refund: Money get() = HiringFee.REFUND
}
