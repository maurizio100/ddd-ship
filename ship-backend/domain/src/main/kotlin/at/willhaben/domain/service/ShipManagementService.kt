package at.willhaben.domain.service

import at.willhaben.domain.model.Ship
import at.willhaben.domain.ports.driven.ShipPersistencePort
import at.willhaben.domain.ports.driving.ShipCreationDataDTO
import at.willhaben.domain.ports.driving.ShipDTO
import at.willhaben.domain.ports.driving.ShipManagementPort
import org.springframework.stereotype.Service
import java.lang.IllegalStateException

@Service
class ShipManagementService(
    private val shipPersistencePort: ShipPersistencePort,
): ShipManagementPort {

    override fun createShip(shipCreationData: ShipCreationDataDTO): ShipDTO {
        val newShip = shipPersistencePort.save(Ship(name = shipCreationData.name))
        return ShipDTO(
            id = newShip.id ?: throw IllegalStateException(),
            name = newShip.shipName
        )
    }

    override fun getAllShips(): List<ShipDTO> {
        TODO("Not yet implemented")
    }

    private fun toShipDTO(ship: Ship) =
        ShipDTO(
            id = ship.id ?: throw IllegalStateException(),
            name = ship.shipName
        )
}
