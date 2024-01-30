package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.converter.ShipConverter
import com.sonicdevelopment.domain.exception.ShipTooHeavyException
import com.sonicdevelopment.domain.ports.driven.CargoQueryPort
import com.sonicdevelopment.domain.ports.driven.ShipPersistencePort
import com.sonicdevelopment.domain.ports.driven.ShipQueryPort
import com.sonicdevelopment.domain.ports.driving.cargo.CargoLoadManagementPort
import com.sonicdevelopment.domain.ports.driving.ship.ShipDetailDTO
import org.springframework.stereotype.Service

@Service
class CargoLoadManagementService(
    private val shipQueryPort: ShipQueryPort,
    private val shipPersistencePort: ShipPersistencePort,
    private val cargoQueryPort: CargoQueryPort
) : CargoLoadManagementPort {
    override fun addCargo(shipId: Long, cargoId: Long): ShipDetailDTO? {
        val ship = shipQueryPort.getShipDetails(shipId) ?: return null
        val cargo = cargoQueryPort.findCargo(cargoId) ?: return null

        return try {
            ship.addCargo(cargo)
            shipPersistencePort.save(ship)
            ShipConverter.toShipDetailDTO(ship)
        } catch (she: ShipTooHeavyException) {
           ShipConverter.toShipDetailDTO(ship)
        }
    }

    override fun removeCargo(shipId: Long, cargoId: Long): ShipDetailDTO? {
        val ship = shipQueryPort.getShipDetails(shipId) ?: return null
        val cargo = cargoQueryPort.findCargo(cargoId) ?: return null

        ship.removeCargo(cargo)
        shipPersistencePort.save(ship)

        return ShipConverter.toShipDetailDTO(ship)
    }
}
