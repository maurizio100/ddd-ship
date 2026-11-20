package com.sonicdevelopment.domain.ports.driving.ship

import com.sonicdevelopment.domain.model.values.CrewMemberId
import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.domain.model.values.ShipId

/** A Dismissal: the ship, the Crew Member that left it, the [refund] paid into the Savings and the [savings] after it. */
data class DismissalDTO(
    val shipId: ShipId,
    val shipName: String,
    val crewMemberId: CrewMemberId,
    val refund: Money,
    val savings: Money,
)
