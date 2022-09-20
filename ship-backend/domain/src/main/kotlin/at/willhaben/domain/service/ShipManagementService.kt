package at.willhaben.domain.service

import at.willhaben.domain.converter.ShipConverter
import at.willhaben.domain.model.Ship
import at.willhaben.domain.ports.driven.ShipPersistencePort
import at.willhaben.domain.ports.driven.ShipQueryPort
import at.willhaben.domain.ports.driving.ShipCreationDataDTO
import at.willhaben.domain.ports.driving.ShipDTO
import at.willhaben.domain.ports.driving.ShipManagementPort
import at.willhaben.domain.ports.driving.ShipUpdateDataDTO
import org.springframework.stereotype.Service

@Service
class ShipManagementService(
    private val shipPersistencePort: ShipPersistencePort,
    private val shipQueryPersistencePort: ShipQueryPort
): ShipManagementPort {

    override fun createShip(shipCreationData: ShipCreationDataDTO): ShipDTO {
        val newShip = shipPersistencePort.save(Ship(name = shipCreationData.name))
        return ShipConverter.toShipDTO(newShip)
    }

    override fun deleteShip(shipId: Long) {
        shipPersistencePort.delete(shipId)
    }

    override fun updateShip(shipId: Long, shipUpdateData: ShipUpdateDataDTO): ShipDTO? {
        return shipQueryPersistencePort.getShipDetails(shipId)?.let {
            updateShipData(it, shipUpdateData)
        }
    }

    private fun updateShipData(ship: Ship, shipUpdateData: ShipUpdateDataDTO): ShipDTO {
        ship.shipName = shipUpdateData.name
        return ShipConverter.toShipDTO(shipPersistencePort.save(ship))
    }
}
