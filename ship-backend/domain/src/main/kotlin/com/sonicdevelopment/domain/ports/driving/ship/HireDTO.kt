package com.sonicdevelopment.domain.ports.driving.ship

import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.ports.driving.harbor.RecruitDTO

/** A Hire: the ship, the Crew Member that joined it, the [hiringFee] paid, the [savings] left and the Recruit that took its place in the pool. */
data class HireDTO(
    val shipId: ShipId,
    val shipName: String,
    val crewMember: CrewMemberDTO,
    val hiringFee: Money,
    val savings: Money,
    val newRecruit: RecruitDTO,
)
