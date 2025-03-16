package com.sonicdevelopment.driven.adapter.persistence.shipping

import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.enums.ShippingState
import com.sonicdevelopment.domain.model.values.CargoId
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.model.values.ShippingId
import com.sonicdevelopment.domain.model.values.ShippingQuote
import com.sonicdevelopment.domain.ports.driven.ShippingRepositoryPort
import com.sonicdevelopment.domain.ports.driving.cargo.CargoDTO
import com.sonicdevelopment.domain.ports.driving.shipping.ShippingDetailsDTO
import com.sonicdevelopment.driven.adapter.persistence.cargo.CargoPersistenceEntity
import com.sonicdevelopment.driven.adapter.persistence.ship.ShipPersistenceEntityRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Component

@Component
class ShippingRepositoryAdapter(
    private val shippingRepository: ShippingRepository,
    private val shipPersistenceEntityRepository: ShipPersistenceEntityRepository
): ShippingRepositoryPort {

    @Transactional
    override fun createShipping(ship: Ship) {
        val persistedShip = shipPersistenceEntityRepository.findByShipId(ship.id.id) ?: throw IllegalStateException()
        val shippingRecord = ship.activeShipping ?: throw IllegalStateException()
        val shippingToPersist = ShippingPersistenceEntity(
            shippingId = shippingRecord.id.id,
            ship = persistedShip,
            shppingState = ShippingStateEnumEntity.valueOf(shippingRecord.shippingState.name)
        )

        shippingRepository.save(shippingToPersist)
    }

    @Transactional
    override fun updateActiveShipping(ship: Ship) {
        val shipping = ship.activeShipping ?: throw IllegalStateException()
        val persistedShipping = shippingRepository.findByShippingId(shipping.id.id) ?: throw IllegalStateException()

        persistedShipping.shppingState = ShippingStateEnumEntity.valueOf(shipping.shippingState.name)
        persistedShipping.sailorsCode = shipping.shippingQuote?.quote

        shippingRepository.save(persistedShipping)
    }

    override fun getShippingInformation(shipId: ShipId, shippingId: ShippingId): ShippingDetailsDTO? {
        return shippingRepository.findByShippingIdAndShip_ShipId(shippingId.id, shipId.id)?.let {
            toShippingDetailDTO(it)
        }
    }

    private fun toShippingDetailDTO(shippingPersistenceEntity: ShippingPersistenceEntity) =
        ShippingDetailsDTO(
            shipId = ShipId(shippingPersistenceEntity.ship.shipId),
            shippingId = ShippingId(shippingPersistenceEntity.shippingId),
            shippingState = ShippingState.valueOf(shippingPersistenceEntity.shppingState.name),
            shipName = shippingPersistenceEntity.ship.shipName,
            shippingQuote = shippingPersistenceEntity.sailorsCode?.let { ShippingQuote(it) },
            cargo = shippingPersistenceEntity.cargoLoad.map {
                toCargoDTO(it)
            },
            actualWeight = 0.0f
        )

    private fun toCargoDTO(cargo: CargoPersistenceEntity) =
        CargoDTO(
            id = CargoId(cargo.cargoId),
            name = cargo.cargoName,
            weight = cargo.cargoWeight
        )

}
