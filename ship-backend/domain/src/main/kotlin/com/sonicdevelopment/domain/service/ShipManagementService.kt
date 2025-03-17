package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.converter.ShipConverter
import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.ports.driven.CatainRepository
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort.InitialShipInformation.Companion.fromShip
import com.sonicdevelopment.domain.ports.driving.ship.ShipCreationDataDTO
import com.sonicdevelopment.domain.ports.driving.ship.ShipDTO
import com.sonicdevelopment.domain.ports.driving.ship.ShipManagementPort
import com.sonicdevelopment.domain.ports.driving.ship.ShipUpdateDataDTO
import org.springframework.stereotype.Service

@Service
class ShipManagementService(
    private val shipRepositoryPort: ShipRepositoryPort,
    private val catainRepository: CatainRepository
): ShipManagementPort {

    override fun createShip(shipCreationData: ShipCreationDataDTO): ShipDTO {
        val catain = catainRepository.findCatainById(shipCreationData.catainId)
            ?: throw IllegalStateException("cannot find catain with id ${shipCreationData.catainId}")

        val ship = Ship(name = shipCreationData.name, catainId = shipCreationData.catainId, catainName = catain.catainName)
        shipRepositoryPort.saveNewShip(fromShip(ship))

        return ShipConverter.toShipDTO(ship)
    }

    override fun deleteShip(shipId: ShipId) {
        shipRepositoryPort.delete(shipId)
    }

    override fun updateShip(shipId: ShipId, shipUpdateData: ShipUpdateDataDTO): ShipDTO? {
        return shipRepositoryPort.getShipDetails(shipId)?.let {
            updateShipData(it, shipUpdateData)
        }
    }

    private fun updateShipData(ship: Ship, shipUpdateData: ShipUpdateDataDTO): ShipDTO {
        shipUpdateData.name?.apply { ship.shipName = this }
        shipRepositoryPort.saveNewShip(fromShip(ship))
        return ShipConverter.toShipDTO(ship)
    }
}
