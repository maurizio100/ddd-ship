package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.converter.ShipConverter
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.ports.driven.QuoteRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShippingRepositoryPort
import com.sonicdevelopment.domain.ports.driving.ship.ShipDetailDTO
import com.sonicdevelopment.domain.ports.driving.shipping.ShippingManagementPort
import org.springframework.stereotype.Service

@Service
class ShippingManagementService(
    private val shipRepositoryPort: ShipRepositoryPort,
    private val shippingRepositoryPort: ShippingRepositoryPort,
    private val quoteRepositoryPort: QuoteRepositoryPort
): ShippingManagementPort {
    override fun createShipping(shipId: ShipId): ShipDetailDTO? {
        val foundShip = shipRepositoryPort.getShipDetails(shipId) ?: return null

        foundShip.createNewShipping()

        val shipWithShippingRecord = shippingRepositoryPort.createShipping(foundShip)
        return ShipConverter.toShipDetailDTO(shipWithShippingRecord)
    }

    override fun releaseShipping(shipId: ShipId): ShipDetailDTO? {
        val foundShip = shipRepositoryPort.getShipDetails(shipId) ?: return null
        val quoteForSailorsCode = quoteRepositoryPort.getQuoteForSailorsCode(foundShip.createSailorsCode())

        foundShip.release(quoteForSailorsCode)

        shippingRepositoryPort.updateActiveShipping(foundShip)
        return ShipConverter.toShipDetailDTO(foundShip)
    }
}
