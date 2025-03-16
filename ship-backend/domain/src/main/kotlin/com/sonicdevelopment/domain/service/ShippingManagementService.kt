package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.converter.ShippingConverter
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.ports.driven.QuoteRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShippingRepositoryPort
import com.sonicdevelopment.domain.ports.driving.shipping.ShippingDetailsDTO
import com.sonicdevelopment.domain.ports.driving.shipping.ShippingManagementPort
import org.springframework.stereotype.Service

@Service
class ShippingManagementService(
    private val shipRepositoryPort: ShipRepositoryPort,
    private val shippingRepositoryPort: ShippingRepositoryPort,
    private val quoteRepositoryPort: QuoteRepositoryPort
): ShippingManagementPort {
    override fun createShipping(shipId: ShipId): ShippingDetailsDTO? {
        val foundShip = shipRepositoryPort.getShipDetails(shipId) ?: return null

        foundShip.createNewShipping()
        shippingRepositoryPort.createShipping(foundShip)

        return foundShip.activeShipping?.let {
            ShippingConverter.toShippingDetailDTO(foundShip, it)
        } ?: throw IllegalStateException()
    }

    override fun releaseShipping(shipId: ShipId): ShippingDetailsDTO? {
        val foundShip = shipRepositoryPort.getShipDetails(shipId) ?: return null
        val quoteForSailorsCode = quoteRepositoryPort.getQuoteForSailorsCode(foundShip.createSailorsCode())

        foundShip.release(quoteForSailorsCode)
        shippingRepositoryPort.updateActiveShipping(foundShip)

        return foundShip.activeShipping?.let {
            ShippingConverter.toShippingDetailDTO(foundShip, it)
        }
    }
}
