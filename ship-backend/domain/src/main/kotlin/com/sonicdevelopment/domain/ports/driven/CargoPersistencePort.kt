package com.sonicdevelopment.domain.ports.driven

import com.sonicdevelopment.domain.model.Cargo
import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.values.ShippingId

interface CargoPersistencePort {
    fun updateCargoLoad(cargoLoadInformation: CargoLoadInformation): CargoLoadInformation

    class CargoLoadInformation private constructor(
        val shippingId: ShippingId,
        val cargoLoad: List<Cargo>
    ) {
        companion object {
            fun fromShip(ship: Ship) = CargoLoadInformation(
                shippingId = ship.activeShipping?.id ?: throw IllegalStateException(),
                cargoLoad = ship.loadedCargo
            )
        }
    }
}