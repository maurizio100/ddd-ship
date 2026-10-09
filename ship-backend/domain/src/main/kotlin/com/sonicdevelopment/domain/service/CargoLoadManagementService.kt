package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.converter.ShipConverter
import com.sonicdevelopment.domain.exception.CargoOutOfStockException
import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.values.CargoId
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.ports.driven.CargoPersistencePort
import com.sonicdevelopment.domain.ports.driven.CargoQueryPort
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort
import com.sonicdevelopment.domain.ports.driven.StockRepositoryPort
import com.sonicdevelopment.domain.ports.driving.cargo.CargoLoadManagementPort
import com.sonicdevelopment.domain.ports.driving.ship.ShipDetailDTO
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class CargoLoadManagementService(
    private val shipRepositoryPort: ShipRepositoryPort,
    private val cargoPersistencePort: CargoPersistencePort,
    private val cargoQueryPort: CargoQueryPort,
    private val stockRepositoryPort: StockRepositoryPort
) : CargoLoadManagementPort {

    /**
     * Loads the Cargo and takes one out of the Stock, in one transaction: a load rejected by the ship
     * (Max Weight) or by the Stock (out of Stock) leaves both unchanged.
     */
    @Transactional
    override fun addCargo(shipId: ShipId, cargoId: CargoId): ShipDetailDTO? {
        val ship = shipRepositoryPort.getShipDetails(shipId) ?: return null
        val cargo = cargoQueryPort.findCargo(cargoId) ?: return null

        ship.addCargo(cargo)
        if (!stockRepositoryPort.takeOneFromStock(cargo.id)) {
            throw CargoOutOfStockException("${cargo.name} is out of Stock")
        }
        cargoPersistencePort.updateCargoLoad(toCargoLoadInformation(ship))

        return ShipConverter.toShipDetailDTO(ship)
    }

    /** Unloads the Cargo and, only if it was on board, puts it back into the Stock. */
    @Transactional
    override fun removeCargo(shipId: ShipId, cargoId: CargoId): ShipDetailDTO? {
        val ship = shipRepositoryPort.getShipDetails(shipId) ?: return null
        val cargo = cargoQueryPort.findCargo(cargoId) ?: return null

        if (ship.removeCargo(cargo)) {
            stockRepositoryPort.putIntoStock(cargo.id)
        }
        cargoPersistencePort.updateCargoLoad(toCargoLoadInformation(ship))

        return ShipConverter.toShipDetailDTO(ship)
    }

    private fun toCargoLoadInformation(ship: Ship) = CargoPersistencePort.CargoLoadInformation.fromShip(ship)
}
