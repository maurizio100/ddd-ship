package com.sonicdevelopment.driving.adapter.web.responsemodel

import java.util.*

data class ShippingResponse(
    val id: UUID,
    val name: String,
    val cargo: List<CargoResponse>,
    val sailorsCode: String?
)
