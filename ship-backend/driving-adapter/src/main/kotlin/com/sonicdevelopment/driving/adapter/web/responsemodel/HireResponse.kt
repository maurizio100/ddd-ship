package com.sonicdevelopment.driving.adapter.web.responsemodel

import java.util.UUID

/** A Hire: the ship, the Crew Member that joined it, the [hiringFee] paid and the [savings] left (decimal strings, ADR-0008), and the Recruit that took its place. */
data class HireResponse(
    val shipId: UUID,
    val name: String,
    val crewMember: CrewMemberResponse,
    val hiringFee: String,
    val savings: String,
    val newRecruit: RecruitResponse,
)
