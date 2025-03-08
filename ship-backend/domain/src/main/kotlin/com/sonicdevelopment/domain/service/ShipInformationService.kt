package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.converter.ShipConverter
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort
import com.sonicdevelopment.domain.ports.driving.ship.ShipDTO
import com.sonicdevelopment.domain.ports.driving.ship.ShipDetailDTO
import com.sonicdevelopment.domain.ports.driving.ship.ShipInformationPort
import org.springframework.stereotype.Service

@Service
class ShipInformationService(
    private val shipRepositoryPort: ShipRepositoryPort
): ShipInformationPort {

    override fun getAllShips(): List<ShipDTO> {
        return shipRepositoryPort.getAllShips().map { ShipConverter.toShipDTO(it)}
    }

    override fun getShipDetails(shipId: ShipId): ShipDetailDTO? {
        return shipRepositoryPort.getShipDetails(shipId)?.let {
            ShipConverter.toShipDetailDTO(it)
        }
    }
}
