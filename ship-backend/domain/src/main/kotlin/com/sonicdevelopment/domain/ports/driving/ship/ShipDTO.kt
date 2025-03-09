package com.sonicdevelopment.domain.ports.driving.ship

import com.sonicdevelopment.domain.model.values.ShipId
import java.util.UUID

data class ShipDTO(
    val id: ShipId,
    val name: String
) {
}
