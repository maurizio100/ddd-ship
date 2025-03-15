package com.sonicdevelopment.driving.adapter.web.responsemodel

import java.util.*

data class ShipOverviewResponse(
    val id: UUID,
    val name: String,
    val hasActiveShipping: Boolean
)
