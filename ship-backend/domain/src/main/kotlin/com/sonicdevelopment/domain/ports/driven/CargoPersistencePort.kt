package com.sonicdevelopment.domain.ports.driven

import com.sonicdevelopment.domain.model.Cargo

interface CargoPersistencePort {
    fun updateCargoLoad(cargoLoadInformation: CargoLoadInformation): CargoLoadInformation

    class CargoLoadInformation(
        val shipId: Long?,
        val cargoLoad: List<Cargo>
    )
}