package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.converter.ShipConverter
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort
import com.sonicdevelopment.domain.ports.driving.ship.ShipDetailDTO
import com.sonicdevelopment.domain.ports.driving.shipping.ShippingInformationPort
import org.springframework.stereotype.Service

@Service
class ShippingInformationService(
    private val shipRepositoryPort: ShipRepositoryPort
): ShippingInformationPort {
    override fun getShipping(shipId: ShipId): ShipDetailDTO? {
        return shipRepositoryPort.getShipDetails(shipId)?.let {
            ShipConverter.toShipDetailDTO(it)
        }
    }
}
