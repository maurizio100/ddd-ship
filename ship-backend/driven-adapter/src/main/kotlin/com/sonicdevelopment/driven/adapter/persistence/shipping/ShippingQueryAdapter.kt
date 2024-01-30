package com.sonicdevelopment.driven.adapter.persistence.shipping

import at.willhaben.domain.model.Cargo
import at.willhaben.domain.model.Ship
import at.willhaben.domain.model.Shipping
import at.willhaben.domain.ports.driven.ShippingQueryPort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class ShippingQueryAdapter(
    private val shippingRepository: ShippingRepository
): ShippingQueryPort {
    override fun getShipForShippingId(shippingId: Long): Ship? {
        return shippingRepository.findByIdOrNull(shippingId)?.let {
            toShip(it)
        }
    }

    private fun toShip(shippingPersistenceEntity: ShippingPersistenceEntity): Ship {
        val persistedShip = shippingPersistenceEntity.ship
        val persistedCargo = persistedShip.cargoLoad

        val ship = Ship(
            id = persistedShip.id,
            name = persistedShip.shipName,
            cargoLoad = persistedCargo.map {
                Cargo(id = it.id, name = it.cargoName, it.cargoWeight)
            }.toMutableList()
        )
        ship.shipping = Shipping(
            id = shippingPersistenceEntity.id,
            sailorsQuote = shippingPersistenceEntity.sailorsCode
        )
        return ship
    }

}
