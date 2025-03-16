package com.sonicdevelopment.domain.converter

import com.sonicdevelopment.domain.model.Cargo
import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.Shipping
import com.sonicdevelopment.domain.ports.driving.cargo.CargoDTO
import com.sonicdevelopment.domain.ports.driving.shipping.ShippingDetailsDTO

object ShippingConverter {
    fun toShippingDetailDTO(ship: Ship, shipping: Shipping) =
        ShippingDetailsDTO(
            shipId = ship.id,
            shippingId = shipping.id,
            shippingState = shipping.shippingState,
            shipName = ship.shipName,
            shippingQuote = shipping.shippingQuote,
            cargo = ship.loadedCargo.map {
                toCargoDTO(it)
            },
            actualWeight = ship.weight

        )

    private fun toCargoDTO(cargo: Cargo) =
        CargoDTO(
            id = cargo.id,
            name = cargo.name,
            weight = cargo.weight
        )
}
