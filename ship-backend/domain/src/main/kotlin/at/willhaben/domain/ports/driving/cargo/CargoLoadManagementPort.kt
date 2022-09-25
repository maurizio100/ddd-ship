package at.willhaben.domain.ports.driving.cargo

import at.willhaben.domain.ports.driving.ship.ShipDTO
import at.willhaben.domain.ports.driving.ship.ShipDetailDTO

interface CargoLoadManagementPort {
    fun addCargo(shipId: Long, cargoId: Long): ShipDetailDTO?
}
