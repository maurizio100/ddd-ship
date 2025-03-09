package com.sonicdevelopment.domain.ports.driving.ship

import com.sonicdevelopment.domain.model.values.ShipId

interface ShipManagementPort {
    fun createShip(shipCreationData: ShipCreationDataDTO): ShipDTO
    fun deleteShip(shipId: ShipId)
    fun updateShip(shipId: ShipId, shipUpdateData: ShipUpdateDataDTO): ShipDTO?
}
