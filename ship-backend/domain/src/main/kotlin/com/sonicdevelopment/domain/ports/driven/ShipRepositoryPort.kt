package com.sonicdevelopment.domain.ports.driven

import com.sonicdevelopment.domain.model.Cargo
import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.values.CatainId
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.ShipId

interface ShipRepositoryPort {
    /**
     * Saves the ship by its Ship Id, in the fleet. Its Incoming flag is written from [InitialShipInformation.incoming]
     * and its Cargo aboard is replaced by [InitialShipInformation.cargoAboard].
     */
    fun saveNewShip(ship: InitialShipInformation)

    class InitialShipInformation private constructor(
        val shipId: ShipId,
        val shipName: String,
        val catainId: CatainId,
        val arrivedFrom: HarborName?,
        val homeHarbor: HarborName,
        val cargoAboard: List<Cargo>,
        val incoming: Boolean,
    ) {
        companion object {
            fun fromShip(ship: Ship) = InitialShipInformation(
                ship.id, shipName = ship.shipName, ship.catainId, ship.arrivedFrom, ship.homeHarbor, ship.cargoAboard, ship.isIncoming
            )
        }
    }
    fun getAllShips(): List<Ship>
    fun getShipDetails(shipId: ShipId): Ship?
    /** Deletes a ship in this Harbor's fleet; returns `false` for a ship that is not (unknown or left). */
    fun delete(shipId: ShipId): Boolean

    /** The ship leaves this Harbor's fleet; its Shippings are kept; runs in the caller's transaction. */
    fun removeFromFleet(shipId: ShipId)

    /**
     * Clears the Incoming flag and the Cargo aboard of an Incoming Ship in the fleet in one conditional step;
     * returns `false`, and changes nothing, when the ship is not Incoming (anymore) or not in the fleet. Runs in
     * the caller's transaction.
     */
    fun unloadIncomingShip(shipId: ShipId): Boolean
}
