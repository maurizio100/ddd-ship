package at.willhaben.domain.service

import at.willhaben.domain.converter.ShipConverter
import at.willhaben.domain.ports.driven.CargoQueryPort
import at.willhaben.domain.ports.driven.ShipPersistencePort
import at.willhaben.domain.ports.driven.ShipQueryPort
import at.willhaben.domain.ports.driving.cargo.CargoLoadManagementPort
import at.willhaben.domain.ports.driving.ship.ShipDetailDTO
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

        ship.addCargo(cargo)
        shipPersistencePort.save(ship)

        return ShipConverter.toShipDetailDTO(ship)
    }

    override fun removeCargo(shipId: Long, cargoId: Long): ShipDetailDTO? {
        TODO("Not yet implemented")
    }
}
