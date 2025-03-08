package com.sonicdevelopment.driven.adapter.persistence.shipping

import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.Shipping
import com.sonicdevelopment.domain.ports.driven.ShippingPersistencePort
import com.sonicdevelopment.driven.adapter.persistence.ship.ShipPersistenceEntityRepository
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class ShippingPersistenceAdapter(
    private val shippingRepository: ShippingRepository,
    private val shipPersistenceEntityRepository: ShipPersistenceEntityRepository
): ShippingPersistencePort {

    @Transactional
    override fun createShipping(ship: Ship): Ship {
        val shippingRecord = ship.shipping ?: throw IllegalStateException()

        val persistedShippingRecord: ShippingPersistenceEntity = shippingRecord.id?.let { shippingRepository.findByIdOrNull(it) }?.let {
            updateShippingRecord(shippingRecord, it)
        } ?: createShippingRecord(ship, shippingRecord)

        shippingRecord.id = persistedShippingRecord.id
        return ship
    }

    private fun createShippingRecord(ship: Ship, shippingRecord: Shipping): ShippingPersistenceEntity {
        val persistedShip = shipPersistenceEntityRepository.findByShipId(ship.id.id) ?: throw IllegalStateException()
        val shippingPersistenceEntity = ShippingPersistenceEntity(
            sailorsCode = shippingRecord.sailorsQuote,
            ship = persistedShip
        )

        val persistedShipping = shippingRepository.save(shippingPersistenceEntity)
        persistedShip.shipping = persistedShipping
        shipPersistenceEntityRepository.save(persistedShip)

        return persistedShipping
    }

    private fun updateShippingRecord(shippingRecord: Shipping, shippingPersistenceEntity: ShippingPersistenceEntity): ShippingPersistenceEntity {
        shippingPersistenceEntity.sailorsCode = shippingRecord.sailorsQuote
        return shippingRepository.save(shippingPersistenceEntity)
    }
}
