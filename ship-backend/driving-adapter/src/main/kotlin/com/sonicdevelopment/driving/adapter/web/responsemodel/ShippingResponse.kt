package com.sonicdevelopment.driving.adapter.web.responsemodel

data class ShippingResponse(
    val id: Long,
    val name: String,
    val cargo: List<CargoResponse>,
    val sailorsCode: String
)
