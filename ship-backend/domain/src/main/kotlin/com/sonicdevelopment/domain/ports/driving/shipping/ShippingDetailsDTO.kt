package com.sonicdevelopment.domain.ports.driving.shipping

import com.sonicdevelopment.domain.model.enums.ShippingState
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.model.values.ShippingId
import com.sonicdevelopment.domain.model.values.ShippingQuote
import com.sonicdevelopment.domain.ports.driving.cargo.CargoDTO

data class ShippingDetailsDTO(
    val shipId: ShipId,
    val shippingId: ShippingId,
    val shipName: String,
    val shippingState: ShippingState,
    val shippingQuote: ShippingQuote?,
    val cargo: List<CargoDTO>,
    val actualWeight: Float
)
