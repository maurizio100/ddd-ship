package at.willhaben.domain.service

import at.willhaben.domain.converter.ShipConverter
import at.willhaben.domain.model.Shipping
import at.willhaben.domain.ports.driven.ShipQueryPort
import at.willhaben.domain.ports.driven.ShippingPersistencePort
import at.willhaben.domain.ports.driving.shipping.ShippingCreationDataDTO
import at.willhaben.domain.ports.driving.shipping.ShippingInformationDTO
import at.willhaben.domain.ports.driving.shipping.ShippingManagementPort
import org.springframework.stereotype.Service

@Service
class ShippingManagementService(
    private val shipQueryPort: ShipQueryPort,
    private val shippingPersistencePort: ShippingPersistencePort
): ShippingManagementPort {
    override fun createShipping(shippingInformation: ShippingCreationDataDTO): ShippingInformationDTO? {
        val foundShip = shipQueryPort.getShipDetails(shippingInformation.shipId) ?: return null
        if (foundShip.shipping != null) return ShipConverter.toShippingInformationDTO(foundShip)

        val shipping = Shipping(
            sailorsCode = "some code"
        )
        foundShip.shipping = shipping

        val shipWithShippingRecord = shippingPersistencePort.createShipping(foundShip)
        return ShipConverter.toShippingInformationDTO(shipWithShippingRecord)
    }
}
