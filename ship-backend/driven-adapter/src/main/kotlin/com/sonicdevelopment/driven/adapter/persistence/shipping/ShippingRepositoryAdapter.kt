package com.sonicdevelopment.driven.adapter.persistence.shipping

import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.Shipping
import com.sonicdevelopment.domain.ports.driven.ShippingRepositoryPort
import com.sonicdevelopment.driven.adapter.persistence.ship.ShipPersistenceEntityRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Component

@Component
class ShippingRepositoryAdapter(
    private val shippingRepository: ShippingRepository,
    private val shipPersistenceEntityRepository: ShipPersistenceEntityRepository
): ShippingRepositoryPort {

    @Transactional
    override fun createShipping(ship: Ship): Ship {
        val shippingRecord = ship.activeShipping ?: throw IllegalStateException()
        val existingShipping =
            shippingRepository.findByShip_ShipIdAndShppingStateIn(ship.id.id, listOf(ShippingStateEnumEntity.PERPARING))

        if (existingShipping != null) return ship
        createShippingRecord(ship, shippingRecord)

        return ship
    }

    private fun createShippingRecord(ship: Ship, shippingRecord: Shipping): ShippingPersistenceEntity {
        val persistedShip = shipPersistenceEntityRepository.findByShipId(ship.id.id) ?: throw IllegalStateException()
        val shippingToPersist = ShippingPersistenceEntity(
            shippingId = shippingRecord.id.id,
            sailorsCode = shippingRecord.shippingQuote?.quote,
            ship = persistedShip,
            shppingState = ShippingStateEnumEntity.valueOf(shippingRecord.shippingState.name)
        )

        val persistedShipping = shippingRepository.save(shippingToPersist)
        return persistedShipping
    }

    @Transactional
    override fun updateActiveShipping(ship: Ship) {
        val shipping = ship.activeShipping ?: throw IllegalStateException()
        val persistedShipping = shippingRepository.findByShippingId(shipping.id.id) ?: throw IllegalStateException()

        persistedShipping.shppingState = ShippingStateEnumEntity.valueOf(shipping.shippingState.name)
        persistedShipping.sailorsCode = shipping.shippingQuote?.quote

        shippingRepository.save(persistedShipping)
    }

}
