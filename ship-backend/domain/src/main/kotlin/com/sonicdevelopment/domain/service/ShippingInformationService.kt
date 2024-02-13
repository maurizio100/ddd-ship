package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.converter.ShipConverter
import com.sonicdevelopment.domain.ports.driven.ShippingQueryPort
import com.sonicdevelopment.domain.ports.driving.shipping.ShippingInformationDTO
import com.sonicdevelopment.domain.ports.driving.shipping.ShippingInformationPort
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
