package com.sonicdevelopment.domain.ports.driven

import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.values.CatainId
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.ShipId

interface ShipRepositoryPort {
    fun saveNewShip(ship: InitialShipInformation)

    class InitialShipInformation private constructor(
        val shipId: ShipId,
        val shipName: String,
        val catainId: CatainId,
        val arrivedFrom: HarborName?
    ) {
        companion object {
            fun fromShip(ship: Ship) = InitialShipInformation(ship.id, shipName = ship.shipName, ship.catainId, ship.arrivedFrom)
        }
    }
    fun getAllShips(): List<Ship>
    fun getShipDetails(shipId: ShipId): Ship?
    /** Deletes a ship in this Harbor's fleet; returns `false` for a ship that is not (unknown or left). */
    fun delete(shipId: ShipId): Boolean

    /** The ship leaves this Harbor's fleet; its Shippings are kept; runs in the caller's transaction. */
    fun removeFromFleet(shipId: ShipId)
}
