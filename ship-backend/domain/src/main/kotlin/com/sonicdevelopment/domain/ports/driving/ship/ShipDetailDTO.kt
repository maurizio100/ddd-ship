package com.sonicdevelopment.domain.ports.driving.ship

import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.ports.driving.cargo.CargoDTO

data class ShipDetailDTO(
    val id: ShipId,
    val name: String,
    val cargo: List<CargoDTO>,
    val actualWeight: Float,
    val maxWeight: Float
) {
}
