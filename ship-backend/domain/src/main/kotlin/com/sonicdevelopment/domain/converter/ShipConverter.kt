package com.sonicdevelopment.domain.converter

import com.sonicdevelopment.domain.model.Cargo
import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.ports.driving.cargo.CargoDTO
import com.sonicdevelopment.domain.ports.driving.ship.ShipDTO
import com.sonicdevelopment.domain.ports.driving.ship.ShipDetailDTO

object ShipConverter {
    fun toShipDTO(ship: Ship) =
        ShipDTO(
            id = ship.id,
            name = ship.shipName,
            shippingState = ship.activeShipping?.shippingState
        )

    fun toShipDetailDTO(ship: Ship) =
        ShipDetailDTO(
            id = ship.id,
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
}
