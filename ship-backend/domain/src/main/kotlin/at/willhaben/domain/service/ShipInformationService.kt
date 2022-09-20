package at.willhaben.domain.service

import at.willhaben.domain.model.Ship
import at.willhaben.domain.ports.driven.ShipQueryPort
import at.willhaben.domain.ports.driving.ShipDetailDTO
import at.willhaben.domain.ports.driving.ShipInformationPort
import org.springframework.stereotype.Service
import java.lang.IllegalStateException

@Service
class ShipInformationService(
    private val shipQueryPort: ShipQueryPort
): ShipInformationPort {
    override fun getShipDetails(shipId: Long): ShipDetailDTO? {
        return shipQueryPort.getShipDetails(shipId)?.let {
            toShipDetailDTO(it)
        }
    }

    private fun toShipDetailDTO(ship: Ship) =
        ShipDetailDTO(
            id = ship.id ?: throw IllegalStateException(),
            name = ship.shipName
        )
}
