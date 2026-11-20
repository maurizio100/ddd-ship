package com.sonicdevelopment.domain.ports.driven

import com.sonicdevelopment.domain.model.Cargo
import com.sonicdevelopment.domain.model.CrewMember
import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.enums.ShipClass
import com.sonicdevelopment.domain.model.values.CatainId
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.domain.model.values.ShipId

interface ShipRepositoryPort {
    /**
     * Saves the ship by its Ship Id, in the fleet. Its Incoming and imprisoned flags are written from
     * [InitialShipInformation.incoming] and [InitialShipInformation.imprisoned], and its Cargo aboard is replaced by
     * [InitialShipInformation.cargoAboard]; its Ship Class is written from [InitialShipInformation.shipClass].
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
        /** The Earnings the ship carries until it reaches its Home Harbor. */
        val earnings: Money,
        /** Whether the ship is an Imprisoned Ship, or sails home as one. */
        val imprisoned: Boolean,
        /** The ship's Ship Class; never changes, so an Arrival restores the one the ship sailed with. */
        val shipClass: ShipClass,
    ) {
        companion object {
            fun fromShip(ship: Ship) = InitialShipInformation(
                ship.id, shipName = ship.shipName, ship.catainId, ship.arrivedFrom, ship.homeHarbor, ship.cargoAboard, ship.isIncoming,
                ship.earnings, ship.isImprisoned, ship.shipClass,
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
     * The conditional step that ends an Incoming Ship's wait, used by both unloading and refusal: clears the
     * Incoming flag and the Cargo aboard of an Incoming Ship in the fleet under the ship's row lock; returns
     * `false`, and changes nothing, when the ship is not Incoming (anymore) or not in the fleet. Runs in the
     * caller's transaction.
     */
    fun unloadIncomingShip(shipId: ShipId): Boolean

    /**
     * The conditional step of a Home Harbor's refusal of its own ship: clears the Incoming flag of an Incoming
     * Ship in the fleet under the ship's row lock and keeps its Cargo aboard; returns `false`, and changes
     * nothing, when the ship is not Incoming (anymore) or not in the fleet. Runs in the caller's transaction.
     */
    fun endIncoming(shipId: ShipId): Boolean

    /** Adds [amount] to the Earnings of a ship in the fleet in one atomic step; runs in the caller's transaction. */
    fun addEarnings(shipId: ShipId, amount: Money)

    /**
     * The ship in this Harbor's fleet, read under its row lock, which is held until the caller's transaction ends;
     * `null` for a ship that is not in the fleet. Runs in the caller's transaction.
     */
    fun lockShip(shipId: ShipId): Ship?

    /**
     * The confiscation's write: clears the Incoming flag, sets the imprisoned flag and deletes the Cargo aboard of a
     * ship in the fleet; fails for a ship that is not in the fleet. Runs in the caller's transaction.
     */
    fun confiscate(shipId: ShipId)

    /**
     * The freeing's write: clears the imprisoned flag of an Imprisoned Ship in the fleet; fails for a ship that is
     * not in the fleet or not imprisoned. Runs in the caller's transaction.
     */
    fun free(shipId: ShipId)

    /** Deletes the ship's Cargo aboard; runs in the caller's transaction. */
    fun clearCargoAboard(shipId: ShipId)

    /** Adds [crewMember] to the Crew of a ship in the fleet; fails for a ship that is not in the fleet. Runs in the caller's transaction. */
    fun addCrewMember(shipId: ShipId, crewMember: CrewMember)
}
