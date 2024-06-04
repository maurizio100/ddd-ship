package com.sonicdevelopment.domain.ports.driven

import com.sonicdevelopment.domain.model.Cargo
import com.sonicdevelopment.domain.model.Ship

interface CargoPersistencePort {
    fun updateCargoLoad(cargoLoadInformation: CargoLoadInformation): CargoLoadInformation

    class CargoLoadInformation private constructor(
        val shipId: Long?,
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