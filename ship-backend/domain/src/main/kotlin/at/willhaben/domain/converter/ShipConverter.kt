package at.willhaben.domain.converter

import at.willhaben.domain.model.Ship
import at.willhaben.domain.ports.driving.ShipDTO
import at.willhaben.domain.ports.driving.ShipDetailDTO
import java.lang.IllegalStateException

object ShipConverter {
    fun toShipDTO(ship: Ship) =
        ShipDTO(
            id = ship.id ?: throw IllegalStateException(),
            name = ship.shipName
        )

    fun toShipDetailDTO(ship: Ship) =
        ShipDetailDTO(
            id = ship.id ?: throw IllegalStateException(),
            name = ship.shipName
        )
}
