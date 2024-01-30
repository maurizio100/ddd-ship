package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.converter.ShipConverter
import com.sonicdevelopment.domain.ports.driven.ShipQueryPort
import com.sonicdevelopment.domain.ports.driving.ship.ShipDTO
import com.sonicdevelopment.domain.ports.driving.ship.ShipDetailDTO
import com.sonicdevelopment.domain.ports.driving.ship.ShipInformationPort
import org.springframework.stereotype.Service

@Service
class ShipInformationService(
    private val shipQueryPort: ShipQueryPort
): ShipInformationPort {

    override fun getAllShips(): List<ShipDTO> {
        return shipQueryPort.getAllShips().map { ShipConverter.toShipDTO(it)}
    }

    override fun getShipDetails(shipId: Long): ShipDetailDTO? {
        return shipQueryPort.getShipDetails(shipId)?.let {
            ShipConverter.toShipDetailDTO(it)
        }
    }
}
