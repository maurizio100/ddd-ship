package com.sonicdevelopment.driven.adapter.persistence.ship

import com.sonicdevelopment.domain.model.Cargo
import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.Shipping
import com.sonicdevelopment.domain.model.enums.ShippingState
import com.sonicdevelopment.domain.model.values.*
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort.InitialShipInformation
import com.sonicdevelopment.driven.adapter.persistence.cargo.CargoPersistenceEntity
import com.sonicdevelopment.driven.adapter.persistence.catain.CatainPersistenceEntity
import com.sonicdevelopment.driven.adapter.persistence.catain.CatainPersistenceEntityRepository
import com.sonicdevelopment.driven.adapter.persistence.shipping.ShippingPersistenceEntity
import com.sonicdevelopment.driven.adapter.persistence.shipping.ShippingRepository
import com.sonicdevelopment.driven.adapter.persistence.shipping.ShippingStateEnumEntity
import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation

@Component
class ShipRepositoryAdapter(
    private val shipPersistenceEntityRepository: ShipPersistenceEntityRepository,
    private val shippingRepository: ShippingRepository,
    private val catainRepository: CatainPersistenceEntityRepository
): ShipRepositoryPort {
    /** Saves the ship by its Ship Id: a known Ship Id (renamed, or back in the fleet) keeps its one row. */
    override fun saveNewShip(ship: InitialShipInformation) {
        val catain = catainRepository.findByCatainId(ship.catainId.id) ?: throw EntityNotFoundException()
        val known = shipPersistenceEntityRepository.findByShipId(ship.shipId.id)
        if (known == null) {
            shipPersistenceEntityRepository.save(createShipEntity(ship, catain))
            return
        }
        known.shipName = ship.shipName
        known.catain = catain
        known.inFleet = true
        shipPersistenceEntityRepository.save(known)
    }

    private fun createShipEntity(ship: InitialShipInformation, catain: CatainPersistenceEntity) =
        ShipPersistenceEntity(
            shipId = ship.shipId.id,
            shipName = ship.shipName,
            catain = catain
        )

    @Transactional
    override fun delete(shipId: ShipId): Boolean {
        shippingRepository.deleteByShip_shipId(shipId.id)
        shipPersistenceEntityRepository.deleteByShipId(shipId.id)
        return true
    }

    @org.springframework.transaction.annotation.Transactional(propagation = Propagation.MANDATORY)
    override fun removeFromFleet(shipId: ShipId) {
        val ship = shipPersistenceEntityRepository.findByShipId(shipId.id) ?: return
        ship.inFleet = false
        shipPersistenceEntityRepository.save(ship)
    }

    override fun getAllShips(): List<Ship> {
        return shipPersistenceEntityRepository.findAllByInFleetTrue().map { toShip(it) }
    }

    override fun getShipDetails(shipId: ShipId): Ship? {
        return shipPersistenceEntityRepository.findByShipIdAndInFleetTrue(shipId.id)?.let {
            toShip(it)
        }
    }

    private fun toShip(shipPersistenceEntity: ShipPersistenceEntity): Ship {
        val shippingPersistenceEntity = shippingRepository.findByShip_ShipIdAndShppingStateIn(
            shipPersistenceEntity.shipId,
            listOf(ShippingStateEnumEntity.PREPARING, ShippingStateEnumEntity.SHIPPING)
        )

        val catainId = CatainId(shipPersistenceEntity.catain.catainId)

        return shippingPersistenceEntity?.let {
            Ship(
                id = ShipId(shipPersistenceEntity.shipId),
                name = shipPersistenceEntity.shipName,
                cargoLoad = it.cargoLoad.associate {
                    cargo -> CargoId(cargo.cargoId) to toCargo(cargo)
                }.toMutableMap(),
                activeShipping = toShipping(it),
                catainId = catainId,
                catainName = shipPersistenceEntity.catain.catainName,

            )
        } ?: Ship(
            id = ShipId(shipPersistenceEntity.shipId),
            name = shipPersistenceEntity.shipName,
            catainId = catainId,
            catainName = shipPersistenceEntity.catain.catainName,

        )
    }

    private fun toCargo(cargoPersistenceEntity: CargoPersistenceEntity) =
        Cargo(
            id = CargoId(cargoPersistenceEntity.cargoId),
            name = cargoPersistenceEntity.cargoName,
            weight = cargoPersistenceEntity.cargoWeight
        )

    private fun toShipping(shippingPersistenceEntity: ShippingPersistenceEntity): Shipping {
        return Shipping(
            id = ShippingId(shippingPersistenceEntity.shippingId),
            shippingQuote = shippingPersistenceEntity.sailorsCode?.let { ShippingQuote(it) },
            shippingState = ShippingState.valueOf(
                shippingPersistenceEntity.shppingState.name
            ),
            destinationHarbor = shippingPersistenceEntity.destinationHarbor?.let { HarborName(it) }
        )
    }
}
