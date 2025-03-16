package com.sonicdevelopment.driven.adapter.persistence.cargo

import com.sonicdevelopment.domain.model.Cargo
import com.sonicdevelopment.domain.model.values.CargoId
import com.sonicdevelopment.domain.ports.driven.CargoQueryPort
import org.springframework.stereotype.Component

@Component
class CargoQueryAdapter(
    private val cargoRepository: CargoRepository
): CargoQueryPort {
    override fun findAvailableCargo() =
        cargoRepository.findAll().map{toCargo(it)}

    override fun findCargo(cargoId: CargoId) =
        cargoRepository.findByCargoId(cargoId.id)?.let { toCargo(it) }

    private fun toCargo(cargo: CargoPersistenceEntity) =
        Cargo(
            id = CargoId(cargo.cargoId),
            name = cargo.cargoName,
            weight = cargo.cargoWeight
        )
}
