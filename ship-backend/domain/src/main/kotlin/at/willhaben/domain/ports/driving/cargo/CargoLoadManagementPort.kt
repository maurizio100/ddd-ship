package at.willhaben.domain.ports.driving.cargo

import at.willhaben.domain.ports.driving.ship.ShipDTO

interface CargoLoadManagementPort {
    fun addCargo(shipId: Long, cargoId: Long): ShipDTO?
}
