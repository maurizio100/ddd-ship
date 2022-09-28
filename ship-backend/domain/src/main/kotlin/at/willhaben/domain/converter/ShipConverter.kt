package at.willhaben.domain.converter

import at.willhaben.domain.model.Cargo
import at.willhaben.domain.model.Ship
import at.willhaben.domain.ports.driving.cargo.CargoDTO
import at.willhaben.domain.ports.driving.ship.ShipDTO
import at.willhaben.domain.ports.driving.ship.ShipDetailDTO
import at.willhaben.domain.ports.driving.shipping.ShippingInformationDTO
import kotlin.IllegalStateException

object ShipConverter {
    fun toShipDTO(ship: Ship) =
        ShipDTO(
            id = ship.id ?: throw IllegalStateException(),
            name = ship.shipName
        )

    fun toShipDetailDTO(ship: Ship) =
        ShipDetailDTO(
            id = ship.id ?: throw IllegalStateException(),
            name = ship.shipName,
            cargo = ship.loadedCargo.map { toCargoDTO(it) },
            actualWeight = ship.weight,
            maxWeight = ship.maxWeight
        )

    private fun toCargoDTO(cargo: Cargo) =
        CargoDTO(
            id = cargo.id,
            name = cargo.name,
            weight = cargo.weight
        )

    fun toShippingInformationDTO(ship: Ship): ShippingInformationDTO {
        val shipping = ship.shipping ?: throw IllegalStateException()
        return ShippingInformationDTO(
            shippingId = shipping.id ?: throw IllegalStateException(),
            shipName = ship.shipName,
            cargo = ship.loadedCargo.map { toCargoDTO(it) },
            sailorsCode = shipping.sailorsCode
        )
    }

}
