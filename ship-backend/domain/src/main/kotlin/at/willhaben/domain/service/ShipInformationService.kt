package at.willhaben.domain.service

import at.willhaben.domain.converter.ShipConverter
import at.willhaben.domain.ports.driven.ShipQueryPort
import at.willhaben.domain.ports.driving.ship.ShipDTO
import at.willhaben.domain.ports.driving.ship.ShipDetailDTO
import at.willhaben.domain.ports.driving.ship.ShipInformationPort
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
