package com.sonicdevelopment.driven.adapter.persistence.ship

import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.ports.driven.ShipPersistencePort
import com.sonicdevelopment.driven.adapter.persistence.cargo.CargoRepository
import com.sonicdevelopment.driven.adapter.persistence.shipping.ShippingRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Component

@Component
class ShipPersistenceAdapter(
    private val shipRepository: ShipRepository,
    private val shippingRepository: ShippingRepository,
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

    @Transactional
    override fun delete(shipId: Long) {
        shippingRepository.deleteByShip_Id(shipId)
        shipRepository.deleteById(shipId)
    }
}
