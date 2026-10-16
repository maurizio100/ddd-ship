package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.converter.ShippingConverter
import com.sonicdevelopment.domain.exception.UnknownHarborException
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.ports.driven.CargoPersistencePort
import com.sonicdevelopment.domain.ports.driven.CargoPersistencePort.CargoLoadInformation
import com.sonicdevelopment.domain.ports.driven.KnownHarborRepositoryPort
import com.sonicdevelopment.domain.ports.driven.QuoteRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShippingOutboxRepository
import com.sonicdevelopment.domain.ports.driven.ShippingRepositoryPort
import com.sonicdevelopment.domain.ports.driving.shipping.ShippingDetailsDTO
import com.sonicdevelopment.domain.ports.driving.shipping.ShippingManagementPort
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class ShippingManagementService(
    private val shipRepositoryPort: ShipRepositoryPort,
    private val shippingRepositoryPort: ShippingRepositoryPort,
    private val quoteRepositoryPort: QuoteRepositoryPort,
    private val shippingOutboxRepository: ShippingOutboxRepository,
    private val currentHarbor: HarborName,
    private val knownHarborRepositoryPort: KnownHarborRepositoryPort,
    private val cargoPersistencePort: CargoPersistencePort,
): ShippingManagementPort {
    override fun createShipping(shipId: ShipId): ShippingDetailsDTO? {
        val foundShip = shipRepositoryPort.getShipDetails(shipId) ?: return null

        foundShip.createNewShipping()
        shippingRepositoryPort.createShipping(foundShip)

        return foundShip.activeShipping?.let {
            ShippingConverter.toShippingDetailDTO(foundShip, it)
        } ?: throw IllegalStateException()
    }

    /**
     * Releases the ship to [destinationHarbor], which must be one of the Known Harbors; otherwise
     * nothing is written. The Shipping and its Shipping Published (with this Harbor as Origin Harbor)
     * are written in one transaction. Cargo that was aboard (refused by the Home Harbor) sails as Loaded Cargo.
     */
    @Transactional
    override fun releaseShipping(shipId: ShipId, destinationHarbor: HarborName): ShippingDetailsDTO? {
        val foundShip = shipRepositoryPort.getShipDetails(shipId) ?: return null
        if (destinationHarbor !in knownHarborRepositoryPort.getKnownHarbors()) {
            throw UnknownHarborException("${destinationHarbor.name} is not a Known Harbor")
        }
        val quoteForSailorsCode = quoteRepositoryPort.getQuoteForSailorsCode(foundShip.createSailorsCode())

        val hadCargoAboard = foundShip.cargoAboard.isNotEmpty()
        foundShip.release(quoteForSailorsCode, destinationHarbor)
        if (hadCargoAboard) {
            cargoPersistencePort.updateCargoLoad(CargoLoadInformation.fromShip(foundShip))
            shipRepositoryPort.clearCargoAboard(foundShip.id)
        }
        shippingRepositoryPort.updateActiveShipping(foundShip)
        shippingOutboxRepository.broadcastShipping(foundShip, currentHarbor)

        return foundShip.activeShipping?.let {
            ShippingConverter.toShippingDetailDTO(foundShip, it)
        }
    }
}
