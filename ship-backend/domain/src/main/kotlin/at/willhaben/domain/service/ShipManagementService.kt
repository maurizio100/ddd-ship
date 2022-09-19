package at.willhaben.domain.service

import at.willhaben.domain.model.Ship
import at.willhaben.domain.ports.driven.ShipPersistencePort
import at.willhaben.domain.ports.driven.ShipQueryPort
import at.willhaben.domain.ports.driving.ShipCreationDataDTO
import at.willhaben.domain.ports.driving.ShipDTO
import at.willhaben.domain.ports.driving.ShipManagementPort
import org.springframework.stereotype.Service
import java.lang.IllegalStateException

@Service
class ShipManagementService(
    private val shipPersistencePort: ShipPersistencePort,
    private val shipQueryPort: ShipQueryPort
): ShipManagementPort {

    override fun createShip(shipCreationData: ShipCreationDataDTO): ShipDTO {
        val newShip = shipPersistencePort.save(Ship(name = shipCreationData.name))
        return toShipDTO(newShip)
    }

    override fun getAllShips(): List<ShipDTO> {
        return shipQueryPort.getAllShips().map {toShipDTO(it)}
    }

    private fun toShipDTO(ship: Ship) =
        ShipDTO(
            id = ship.id ?: throw IllegalStateException(),
            name = ship.shipName
        )
}
