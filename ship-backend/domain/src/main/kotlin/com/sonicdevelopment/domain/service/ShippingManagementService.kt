package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.converter.ShipConverter
import com.sonicdevelopment.domain.model.Shipping
import com.sonicdevelopment.domain.ports.driven.QuoteQueryPort
import com.sonicdevelopment.domain.ports.driven.ShipQueryPort
import com.sonicdevelopment.domain.ports.driven.ShippingPersistencePort
import com.sonicdevelopment.domain.ports.driving.shipping.ShippingCreationDataDTO
import com.sonicdevelopment.domain.ports.driving.shipping.ShippingInformationDTO
import com.sonicdevelopment.domain.ports.driving.shipping.ShippingManagementPort
import org.springframework.stereotype.Service

@Service
class ShippingManagementService(
    private val shipQueryPort: ShipQueryPort,
    private val shippingPersistencePort: ShippingPersistencePort,
    private val quoteQueryPort: QuoteQueryPort
): ShippingManagementPort {
    override fun createShipping(shippingInformation: ShippingCreationDataDTO): ShippingInformationDTO? {
        val foundShip = shipQueryPort.getShipDetails(shippingInformation.shipId) ?: return null
        val sailorsCode = foundShip.sailorsCode

        val shipping = Shipping(
            id = foundShip.shipping?.id,
            sailorsQuote = quoteQueryPort.getQuoteForSailorsCode(sailorsCode)
        )

        foundShip.shipping = shipping

        val shipWithShippingRecord = shippingPersistencePort.createShipping(foundShip)
        return ShipConverter.toShippingInformationDTO(shipWithShippingRecord)
    }
}
