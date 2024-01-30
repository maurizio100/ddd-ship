package com.sonicdevelopment.domain.ports.driving.shipping

import com.sonicdevelopment.domain.ports.driving.cargo.CargoDTO

data class ShippingInformationDTO(
    val shippingId: Long,
    val shipName: String,
    val cargo: List<CargoDTO>,
    val sailorsCode: String
)
