package com.sonicdevelopment.driven.adapter.persistence.cargo

import at.willhaben.domain.model.Cargo
import at.willhaben.domain.ports.driven.CargoQueryPort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class CargoQueryAdapter(
    private val cargoRepository: CargoRepository
): CargoQueryPort {
    override fun findAvailableCargo() =
        cargoRepository.findAll().map{toCargo(it)}

    override fun findCargo(cargoId: Long) =
        cargoRepository.findByIdOrNull(cargoId)?.let { toCargo(it) }

    private fun toCargo(cargo: CargoPersistenceEntity) =
        Cargo(
            id = cargo.id,
            name = cargo.cargoName,
            weight = cargo.cargoWeight
        )
}
