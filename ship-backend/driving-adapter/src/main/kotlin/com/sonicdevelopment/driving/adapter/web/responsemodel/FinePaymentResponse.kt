package com.sonicdevelopment.driving.adapter.web.responsemodel

import java.util.UUID

/** A paid Fine: the freed ship, the [fine] paid and the [savings] left, both decimal strings with two decimals (ADR-0008). */
data class FinePaymentResponse(
    val shipId: UUID,
    val name: String,
    val fine: String,
    val savings: String,
)
