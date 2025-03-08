package com.sonicdevelopment.driven.adapter.persistence.ship

import com.sonicdevelopment.domain.model.Cargo
import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.Shipping
import com.sonicdevelopment.domain.model.values.CatainId
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort.InitialShipInformation
import com.sonicdevelopment.driven.adapter.persistence.cargo.CargoPersistenceEntity
import com.sonicdevelopment.driven.adapter.persistence.catain.CatainPersistenceEntity
import com.sonicdevelopment.driven.adapter.persistence.catain.CatainPersistenceEntityRepository
import com.sonicdevelopment.driven.adapter.persistence.shipping.ShippingRepository
import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.springframework.stereotype.Component

@Component
class ShipRepositoryAdapter(
    private val shipPersistenceEntityRepository: ShipPersistenceEntityRepository,
    private val shippingRepository: ShippingRepository,
    private val catainRepository: CatainPersistenceEntityRepository
): ShipRepositoryPort {
    override fun saveNewShip(ship: InitialShipInformation) {
        val catain = catainRepository.findByCatainId(ship.catainId.id) ?: throw EntityNotFoundException()
        shipPersistenceEntityRepository.save(createShipEntity(ship, catain))
    }

    private fun createShipEntity(ship: InitialShipInformation, catain: CatainPersistenceEntity) =
        ShipPersistenceEntity(
            shipId = ship.shipId.id,
            shipName = ship.shipName,
            catain = catain
        )

    @Transactional
    override fun delete(shipId: ShipId) {
        shippingRepository.deleteByShip_shipId(shipId.id)
        shipPersistenceEntityRepository.deleteByShipId(shipId.id)
    }

    override fun getAllShips(): List<Ship> {
        return shipPersistenceEntityRepository.findAll().map { toShip(it) }
    }

    override fun getShipDetails(shipId: ShipId): Ship? {
        return shipPersistenceEntityRepository.findByShipId(shipId.id)?.let {
            toShip(it)
        }
    }

    private fun toShip(shipPersistenceEntity: ShipPersistenceEntity): Ship {
        val ship = Ship(
            id = ShipId(shipPersistenceEntity.shipId),
            name = shipPersistenceEntity.shipName,
            cargoLoad = shipPersistenceEntity.cargoLoad.associate {
                it.id to toCargo(it)
            }.toMutableMap(),
            catainId = CatainId(shipPersistenceEntity.catain.catainId)
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
