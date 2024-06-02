package com.sonicdevelopment.driven.adapter.persistence.ship

import com.sonicdevelopment.domain.ports.driven.CargoPersistencePort
import com.sonicdevelopment.domain.ports.driven.CargoPersistencePort.CargoLoadInformation
import com.sonicdevelopment.driven.adapter.persistence.cargo.CargoRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class CargoPersistenceAdapter(
    private val shipRepository: ShipRepository,
    private val cargoRepository: CargoRepository
): CargoPersistencePort {

    override fun updateCargoLoad(cargoLoadInformation: CargoLoadInformation): CargoLoadInformation {
        val persistedShip = shipRepository.findByIdOrNull(cargoLoadInformation.shipId) ?: throw IllegalStateException()
        persistedShip.cargoLoad = getCargoData(cargoLoadInformation)
        shipRepository.save(persistedShip)
        return cargoLoadInformation
    }

    private fun getCargoData(cargoLoadInformation: CargoLoadInformation) =
        cargoLoadInformation.cargoLoad.map { cargoRepository.getReferenceById(it.id) }.toMutableList()
}
