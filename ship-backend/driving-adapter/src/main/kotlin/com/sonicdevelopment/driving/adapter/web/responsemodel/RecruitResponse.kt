package com.sonicdevelopment.driving.adapter.web.responsemodel

import java.util.UUID

/** A Recruit of the Recruit Pool. */
data class RecruitResponse(
    val id: UUID,
    val name: String,
    val trait: String,
    /** The Hiring Fee: a decimal string with two decimals. */
    val hiringFee: String
)
