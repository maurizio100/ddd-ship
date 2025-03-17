package com.sonicdevelopment.driving.adapter.web.responsemodel

import java.util.*

data class ShippingResponse(
    val id: UUID,
    val shipId: UUID,
    val catainId: UUID,
    val catainName: String,
    val name: String,
    val cargo: List<CargoResponse>,
    val sailorsCode: String?,
    val weight: Float
)
