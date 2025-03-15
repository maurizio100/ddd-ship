package com.sonicdevelopment.driven.adapter.persistence.cargo

import com.sonicdevelopment.domain.ports.driven.CargoPersistencePort
import com.sonicdevelopment.domain.ports.driven.CargoPersistencePort.CargoLoadInformation
import com.sonicdevelopment.driven.adapter.persistence.ship.ShipPersistenceEntityRepository
import org.springframework.stereotype.Component

@Component
class CargoPersistenceAdapter(
    private val shipPersistenceEntityRepository: ShipPersistenceEntityRepository,
    private val cargoRepository: CargoRepository
): CargoPersistencePort {

    override fun updateCargoLoad(cargoLoadInformation: CargoLoadInformation): CargoLoadInformation {
        val persistedShip = shipPersistenceEntityRepository.findByShipId(cargoLoadInformation.shipId.id) ?: throw IllegalStateException()
        persistedShip.cargoLoad = getCargoData(cargoLoadInformation)
        shipPersistenceEntityRepository.save(persistedShip)
        return cargoLoadInformation
    }

    private fun getCargoData(cargoLoadInformation: CargoLoadInformation) =
        cargoLoadInformation.cargoLoad.mapNotNull {
            cargoRepository.findByCargoId(it.id.id)
        }.toMutableList()
}
