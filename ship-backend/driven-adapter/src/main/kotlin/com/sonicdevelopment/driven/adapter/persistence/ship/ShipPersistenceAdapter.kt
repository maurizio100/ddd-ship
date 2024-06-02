package com.sonicdevelopment.driven.adapter.persistence.ship

import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.ports.driven.ShipPersistencePort
import com.sonicdevelopment.driven.adapter.persistence.cargo.CargoRepository
import com.sonicdevelopment.driven.adapter.persistence.shipping.ShippingRepository
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class ShipPersistenceAdapter(
    private val shipRepository: ShipRepository,
    private val shippingRepository: ShippingRepository,
    private val cargoRepository: CargoRepository
): ShipPersistencePort {
    override fun saveNewShip(ship: Ship): Ship {
        val persistedShip = shipRepository.save(createShipEntity(ship))
        return ship.apply { id = persistedShip.id }
    }

    private fun createShipEntity(ship: Ship) =
        ShipPersistenceEntity(
            id = ship.id,
            shipName = ship.shipName
        )

    override fun saveCargoLoad(ship: Ship): Ship {
        val persistedShip = shipRepository.findByIdOrNull(ship.id) ?: throw IllegalStateException()
        persistedShip.cargoLoad = getCargoData(ship)
        shipRepository.save(persistedShip)
        return ship
    }

    private fun getCargoData(ship: Ship) =
        ship.loadedCargo.map { cargoRepository.getReferenceById(it.id) }.toMutableList()

    @Transactional
    override fun delete(shipId: Long) {
        shippingRepository.deleteByShip_Id(shipId)
        shipRepository.deleteById(shipId)
    }
}
