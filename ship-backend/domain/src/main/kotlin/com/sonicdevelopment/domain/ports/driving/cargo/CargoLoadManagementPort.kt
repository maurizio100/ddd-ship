package com.sonicdevelopment.domain.ports.driving.cargo

import com.sonicdevelopment.domain.ports.driving.ship.ShipDetailDTO

interface CargoLoadManagementPort {
    fun addCargo(shipId: Long, cargoId: Long): ShipDetailDTO?
    fun removeCargo(shipId: Long, cargoId: Long): ShipDetailDTO?
}
