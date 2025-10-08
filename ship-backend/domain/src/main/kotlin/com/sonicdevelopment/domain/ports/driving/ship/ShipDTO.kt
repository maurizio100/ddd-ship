package com.sonicdevelopment.domain.ports.driving.ship

import com.sonicdevelopment.domain.model.enums.ShippingState
import com.sonicdevelopment.domain.model.values.ShipId

data class ShipDTO(
    val id: ShipId,
    val name: String,
    val catain: String,
    val shippingState: ShippingState?
) {
}
