package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.model.values.ShippingId
import com.sonicdevelopment.domain.ports.driven.ShippingRepositoryPort
import com.sonicdevelopment.domain.ports.driving.shipping.ShippingDetailsDTO
import com.sonicdevelopment.domain.ports.driving.shipping.ShippingInformationPort
import org.springframework.stereotype.Service

@Service
class ShippingInformationService(
    private val shippingRepositoryPort: ShippingRepositoryPort
): ShippingInformationPort {

    override fun getShipping(shipId: ShipId, shippingId: ShippingId): ShippingDetailsDTO? {
        return null
    }
}
