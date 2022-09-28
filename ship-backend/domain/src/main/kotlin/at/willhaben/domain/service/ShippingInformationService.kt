package at.willhaben.domain.service

import at.willhaben.domain.converter.ShipConverter
import at.willhaben.domain.ports.driven.ShippingQueryPort
import at.willhaben.domain.ports.driving.shipping.ShippingInformationDTO
import at.willhaben.domain.ports.driving.shipping.ShippingInformationPort
import org.springframework.stereotype.Service

@Service
class ShippingInformationService(
    private val shippingQueryPort: ShippingQueryPort
): ShippingInformationPort {
    override fun getShipping(shippingId: Long): ShippingInformationDTO? {
        return shippingQueryPort.getShipForShippingId(shippingId)?.let {
            ShipConverter.toShippingInformationDTO(it)
        }
    }
}
