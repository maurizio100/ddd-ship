package com.sonicdevelopment.driven.adapter.persistence.ship

import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.ports.driven.ShipPersistencePort
import com.sonicdevelopment.driven.adapter.persistence.cargo.CargoRepository
import org.springframework.stereotype.Component

@Component
class ShipPersistenceAdapter(
    private val shipRepository: ShipRepository,
    private val cargoRepository: CargoRepository
): ShipPersistencePort {
    override fun save(ship: Ship): Ship {
        val cargo = ship.loadedCargo.map {
            cargoRepository.getReferenceById(it.id)
        }.toMutableList()

        val shipToPersist = ShipPersistenceEntity(
            id = ship.id,
            shipName = ship.shipName,
            cargoLoad = cargo,
            shipping = null
        )
        shipRepository.save(shipToPersist)
        return ship.apply {
            id = shipToPersist.id
        }
    }

    override fun delete(shipId: Long) {
        shipRepository.deleteById(shipId)
    }
}
