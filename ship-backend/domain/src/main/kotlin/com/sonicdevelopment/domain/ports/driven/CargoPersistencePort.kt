package com.sonicdevelopment.domain.ports.driven

import com.sonicdevelopment.domain.model.Cargo
import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.values.ShipId

interface CargoPersistencePort {
    fun updateCargoLoad(cargoLoadInformation: CargoLoadInformation): CargoLoadInformation

    class CargoLoadInformation private constructor(
        val shipId: ShipId,
        val cargoLoad: List<Cargo>
    ) {
        companion object {
            fun fromShip(ship: Ship) = CargoLoadInformation(
                shipId = ship.id,
                cargoLoad = ship.loadedCargo
            )
        }
    }
}