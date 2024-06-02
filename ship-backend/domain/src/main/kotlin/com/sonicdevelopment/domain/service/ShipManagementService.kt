package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.converter.ShipConverter
import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.ports.driven.ShipPersistencePort
import com.sonicdevelopment.domain.ports.driven.ShipQueryPort
import com.sonicdevelopment.domain.ports.driving.ship.ShipCreationDataDTO
import com.sonicdevelopment.domain.ports.driving.ship.ShipDTO
import com.sonicdevelopment.domain.ports.driving.ship.ShipManagementPort
import com.sonicdevelopment.domain.ports.driving.ship.ShipUpdateDataDTO
import org.springframework.stereotype.Service

@Service
class ShipManagementService(
    private val shipPersistencePort: ShipPersistencePort,
    private val shipQueryPersistencePort: ShipQueryPort
): ShipManagementPort {

    override fun createShip(shipCreationData: ShipCreationDataDTO): ShipDTO {
        val ship = Ship(name = shipCreationData.name)
        val newShip = shipPersistencePort.saveNewShip(ship)
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
        return ShipConverter.toShipDTO(shipPersistencePort.saveNewShip(ship))
    }
}
