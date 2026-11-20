package com.sonicdevelopment.driving.adapter.web.responsemodel

import java.util.UUID

data class DismissalResponse(
    val shipId: UUID,
    val name: String,
    val crewMemberId: UUID,
    val refund: String,
    val savings: String,
)
