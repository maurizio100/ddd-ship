package at.willhaben.domain.service

import at.willhaben.domain.converter.ShipConverter
import at.willhaben.domain.model.Ship
import at.willhaben.domain.ports.driven.ShipPersistencePort
import at.willhaben.domain.ports.driven.ShipQueryPort
import at.willhaben.domain.ports.driving.ship.ShipCreationDataDTO
import at.willhaben.domain.ports.driving.ship.ShipDTO
import at.willhaben.domain.ports.driving.ship.ShipManagementPort
import at.willhaben.domain.ports.driving.ship.ShipUpdateDataDTO
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
        shipUpdateData.name?.apply { ship.shipName = this }
        return ShipConverter.toShipDTO(shipPersistencePort.save(ship))
    }
}
