package com.sonicdevelopment.driven.adapter.persistence.ship

import com.sonicdevelopment.domain.model.Cargo
import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.Shipping
import com.sonicdevelopment.domain.ports.driven.ShipQueryPort
import com.sonicdevelopment.driven.adapter.persistence.cargo.CargoPersistenceEntity
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class ShipQueryAdapter(
    private val shipRepository: ShipRepository
) : ShipQueryPort {

    override fun getAllShips(): List<Ship> {
        return shipRepository.findAll().map { toShip(it) }
    }

    override fun getShipDetails(shipId: Long): Ship? {
        return shipRepository.findByIdOrNull(shipId)?.let {
            toShip(it)
        }
    }

    private fun toShip(shipPersistenceEntity: ShipPersistenceEntity): Ship {
        val ship = Ship(
            id = shipPersistenceEntity.id,
            name = shipPersistenceEntity.shipName,
            cargoLoad = shipPersistenceEntity.cargoLoad.associate {
                it.id to toCargo(it)
            }.toMutableMap(),
        )
        shipPersistenceEntity.shipping?.apply {
            ship.shipping = Shipping(id = this.id, sailorsQuote = this.sailorsCode)
        }

        return ship
    }

    private fun toCargo(cargoPersistenceEntity: CargoPersistenceEntity) =
        Cargo(
            id = cargoPersistenceEntity.id,
            name = cargoPersistenceEntity.cargoName,
            weight = cargoPersistenceEntity.cargoWeight
        )
}

