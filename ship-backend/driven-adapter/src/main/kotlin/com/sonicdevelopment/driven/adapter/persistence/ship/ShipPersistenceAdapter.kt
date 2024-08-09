package com.sonicdevelopment.driven.adapter.persistence.ship

import com.sonicdevelopment.domain.ports.driven.ShipPersistencePort
import com.sonicdevelopment.domain.ports.driven.ShipPersistencePort.InitialShipInformation
import com.sonicdevelopment.driven.adapter.persistence.catain.CatainPersistenceEntity
import com.sonicdevelopment.driven.adapter.persistence.catain.CatainRepository
import com.sonicdevelopment.driven.adapter.persistence.shipping.ShippingRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Component

@Component
class ShipPersistenceAdapter(
    private val shipRepository: ShipRepository,
    private val shippingRepository: ShippingRepository,
    private val catainRepository: CatainRepository
): ShipPersistencePort {
    override fun saveNewShip(ship: InitialShipInformation): Long? {
        val catain = catainRepository.getReferenceById(ship.catainId.id)
        val persistedShip = shipRepository.save(createShipEntity(ship, catain))
        return persistedShip.id
    }

    private fun createShipEntity(ship: InitialShipInformation, catain: CatainPersistenceEntity) =
        ShipPersistenceEntity(
            shipName = ship.shipName,
            catain = catain
        )

    @Transactional
    override fun delete(shipId: Long) {
        shippingRepository.deleteByShip_Id(shipId)
        shipRepository.deleteById(shipId)
    }
}
