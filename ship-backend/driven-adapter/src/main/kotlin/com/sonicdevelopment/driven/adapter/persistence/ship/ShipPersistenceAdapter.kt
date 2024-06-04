package com.sonicdevelopment.driven.adapter.persistence.ship

import com.sonicdevelopment.domain.ports.driven.ShipPersistencePort
import com.sonicdevelopment.domain.ports.driven.ShipPersistencePort.InitialShipInformation
import com.sonicdevelopment.driven.adapter.persistence.shipping.ShippingRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Component

@Component
class ShipPersistenceAdapter(
    private val shipRepository: ShipRepository,
    private val shippingRepository: ShippingRepository
): ShipPersistencePort {
    override fun saveNewShip(ship: InitialShipInformation): Long? {
        val persistedShip = shipRepository.save(createShipEntity(ship))
        return persistedShip.id
    }

    private fun createShipEntity(ship: InitialShipInformation) =
        ShipPersistenceEntity(
            shipName = ship.shipName
        )

    @Transactional
    override fun delete(shipId: Long) {
        shippingRepository.deleteByShip_Id(shipId)
        shipRepository.deleteById(shipId)
    }
}
