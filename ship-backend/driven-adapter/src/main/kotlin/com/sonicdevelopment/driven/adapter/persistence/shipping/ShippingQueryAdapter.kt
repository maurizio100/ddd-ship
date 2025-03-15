package com.sonicdevelopment.driven.adapter.persistence.shipping

import com.sonicdevelopment.domain.model.Cargo
import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.Shipping
import com.sonicdevelopment.domain.model.values.CargoId
import com.sonicdevelopment.domain.model.values.CatainId
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.ports.driven.ShippingQueryPort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.util.*

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
            id = ShipId(persistedShip.shipId),
            name = persistedShip.shipName,
            cargoLoad = persistedCargo.associate {
                CargoId(it.cargoId) to Cargo(id = CargoId(it.cargoId), name = it.cargoName, it.cargoWeight)
            }.toMutableMap(),
            catainId = CatainId(persistedShip.catain?.catainId ?: UUID.randomUUID())
        )
        ship.shipping = Shipping(
            id = shippingPersistenceEntity.id,
            sailorsQuote = shippingPersistenceEntity.sailorsCode
        )
        return ship
    }

}
