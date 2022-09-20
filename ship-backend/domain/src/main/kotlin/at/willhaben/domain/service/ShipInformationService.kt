package at.willhaben.domain.service

import at.willhaben.domain.converter.ShipConverter
import at.willhaben.domain.model.Ship
import at.willhaben.domain.ports.driven.ShipQueryPort
import at.willhaben.domain.ports.driving.ShipDTO
import at.willhaben.domain.ports.driving.ShipDetailDTO
import at.willhaben.domain.ports.driving.ShipInformationPort
import org.springframework.stereotype.Service
import java.lang.IllegalStateException

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
