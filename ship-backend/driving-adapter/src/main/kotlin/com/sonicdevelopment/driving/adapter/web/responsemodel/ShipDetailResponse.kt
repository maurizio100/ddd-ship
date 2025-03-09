package com.sonicdevelopment.driving.adapter.web.responsemodel

import java.util.*

data class ShipDetailResponse(
    val id: UUID,
    val name: String,
    val cargo: List<CargoResponse> = listOf(),
    val weight: Float = 0.0F,
    val maxweight: Float = 0.0F,
)
