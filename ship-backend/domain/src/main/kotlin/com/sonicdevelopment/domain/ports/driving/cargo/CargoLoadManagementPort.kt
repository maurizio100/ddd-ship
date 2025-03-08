package com.sonicdevelopment.domain.ports.driving.cargo

import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.ports.driving.ship.ShipDetailDTO

interface CargoLoadManagementPort {
    fun addCargo(shipId: ShipId, cargoId: Long): ShipDetailDTO?
    fun removeCargo(shipId: ShipId, cargoId: Long): ShipDetailDTO?
}
