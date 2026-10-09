package com.sonicdevelopment.driven.adapter.persistence.ship

import com.sonicdevelopment.domain.model.Cargo
import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.Shipping
import com.sonicdevelopment.domain.model.enums.ShippingState
import com.sonicdevelopment.domain.model.values.*
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort.InitialShipInformation
import com.sonicdevelopment.driven.adapter.persistence.cargo.CargoPersistenceEntity
import com.sonicdevelopment.driven.adapter.persistence.cargo.CargoRepository
import com.sonicdevelopment.driven.adapter.persistence.catain.CatainPersistenceEntity
import com.sonicdevelopment.driven.adapter.persistence.catain.CatainPersistenceEntityRepository
import com.sonicdevelopment.driven.adapter.persistence.shipping.ShippingPersistenceEntity
import com.sonicdevelopment.driven.adapter.persistence.shipping.ShippingRepository
import com.sonicdevelopment.driven.adapter.persistence.shipping.ShippingStateEnumEntity
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class ShipRepositoryAdapter(
    private val shipPersistenceEntityRepository: ShipPersistenceEntityRepository,
    private val shippingRepository: ShippingRepository,
    private val catainRepository: CatainPersistenceEntityRepository,
    private val cargoRepository: CargoRepository,
    private val cargoAboardRepository: ShipCargoAboardPersistenceEntityRepository,
): ShipRepositoryPort {
    /**
     * Saves the ship by its Ship Id: a known Ship Id (renamed, or back in the fleet) keeps its one row.
     * The Home Harbor, the Origin Harbor it arrived from, the Incoming flag and the Cargo aboard are written from the ship:
     * an Arrival overwrites them, and a rename keeps them because the loaded ship carries them. The ship's
     * Cargo aboard rows are replaced, in one transaction with the ship row (joining the caller's, if any).
     */
    @Transactional
    override fun saveNewShip(ship: InitialShipInformation) {
        val catain = catainRepository.findByCatainId(ship.catainId.id) ?: throw EntityNotFoundException()
        val cargoAboard = ship.cargoAboard.map {
            cargoRepository.findByCargoId(it.id.id) ?: throw EntityNotFoundException("Unknown Cargo ${it.id.id}")
        }
        val known = shipPersistenceEntityRepository.findByShipId(ship.shipId.id)
        val saved = if (known == null) {
            shipPersistenceEntityRepository.save(createShipEntity(ship, catain))
        } else {
            known.shipName = ship.shipName
            known.catain = catain
            known.inFleet = true
            known.arrivedFrom = ship.arrivedFrom?.name
            known.homeHarbor = ship.homeHarbor.name
            known.incoming = ship.incoming
            shipPersistenceEntityRepository.save(known)
        }
        cargoAboardRepository.deleteAllByShip_Id(saved.id!!)
        cargoAboardRepository.saveAll(cargoAboard.map { ShipCargoAboardPersistenceEntity(ship = saved, cargo = it) })
    }

    private fun createShipEntity(ship: InitialShipInformation, catain: CatainPersistenceEntity) =
        ShipPersistenceEntity(
            shipId = ship.shipId.id,
            shipName = ship.shipName,
            catain = catain,
            arrivedFrom = ship.arrivedFrom?.name,
            homeHarbor = ship.homeHarbor.name,
            incoming = ship.incoming
        )

    /** Only a ship in the fleet is deleted; a ship that left keeps its row and its Shippings as history. */
    @Transactional
    override fun delete(shipId: ShipId): Boolean {
        shipPersistenceEntityRepository.findByShipIdAndInFleetTrue(shipId.id) ?: return false
        shippingRepository.deleteByShip_shipId(shipId.id)
        cargoAboardRepository.deleteAllByShip_ShipId(shipId.id)
        shipPersistenceEntityRepository.deleteByShipId(shipId.id)
        return true
    }

    @Transactional(propagation = Propagation.MANDATORY)
    override fun removeFromFleet(shipId: ShipId) {
        val ship = shipPersistenceEntityRepository.findByShipId(shipId.id) ?: return
        ship.inFleet = false
        shipPersistenceEntityRepository.save(ship)
    }

    /** The conditional `UPDATE` comes first; the Cargo aboard rows are deleted only if it cleared the ship. */
    @Transactional(propagation = Propagation.MANDATORY)
    override fun unloadIncomingShip(shipId: ShipId): Boolean {
        if (shipPersistenceEntityRepository.unloadIncoming(shipId.id) == 0) return false
        cargoAboardRepository.deleteAllByShip_ShipId(shipId.id)
        return true
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
        val arrivedFrom = shipPersistenceEntity.arrivedFrom?.let { HarborName(it) }
        val homeHarbor = HarborName(shipPersistenceEntity.homeHarbor)
        val cargoAboard = cargoAboardRepository.findAllByShip_IdOrderById(shipPersistenceEntity.id!!).map { toCargo(it.cargo) }

        return shippingPersistenceEntity?.let {
            Ship(
                id = ShipId(shipPersistenceEntity.shipId),
                name = shipPersistenceEntity.shipName,
                cargoLoad = it.cargoLoad.map { cargo -> toCargo(cargo) }.toMutableList(),
                activeShipping = toShipping(it),
                catainId = catainId,
                catainName = shipPersistenceEntity.catain.catainName,
                homeHarbor = homeHarbor,
                arrivedFrom = arrivedFrom,
                cargoAboard = cargoAboard,
                incoming = shipPersistenceEntity.incoming,
            )
        } ?: Ship(
            id = ShipId(shipPersistenceEntity.shipId),
            name = shipPersistenceEntity.shipName,
            catainId = catainId,
            catainName = shipPersistenceEntity.catain.catainName,
            homeHarbor = homeHarbor,
            arrivedFrom = arrivedFrom,
            cargoAboard = cargoAboard,
            incoming = shipPersistenceEntity.incoming,
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
