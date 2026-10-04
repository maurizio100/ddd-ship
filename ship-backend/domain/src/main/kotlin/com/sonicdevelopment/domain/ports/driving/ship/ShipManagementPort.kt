package com.sonicdevelopment.domain.ports.driving.ship

import com.sonicdevelopment.domain.model.values.ShipId

interface ShipManagementPort {
    fun createShip(shipCreationData: ShipCreationDataDTO): ShipDTO
    /** Deletes a ship in the fleet; returns `false` when there is no such ship in the fleet. */
    fun deleteShip(shipId: ShipId): Boolean
    fun updateShip(shipId: ShipId, shipUpdateData: ShipUpdateDataDTO): ShipDTO?
}
