package com.sonicdevelopment.domain.ports.driven

import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.values.HarborName

/**
 * Tells the Users at this Harbor, as it happens, that its fleet changed by itself: a ship arrived, a ship
 * it Released has arrived elsewhere and left the fleet, or the Coast Guard imprisoned a ship here.
 *
 * Called inside the transaction that changes the fleet, an announcement takes effect only once that
 * transaction commits, and never if it rolls back, so a User is never told of a change that did not happen.
 */
interface FleetEventsPort {

    /** [ship] arrived at this Harbor from [originHarbor] and is now in its fleet. */
    fun announceShipArrived(ship: Ship, originHarbor: HarborName)

    /** [ship] arrived at [destinationHarbor] and has left this Harbor's fleet. */
    fun announceShipLeft(ship: Ship, destinationHarbor: HarborName?)

    /** The Coast Guard imprisoned [ship] here: its Cargo is confiscated, and it [sailsHome] or stays imprisoned here. */
    fun announceShipImprisoned(ship: Ship, sailsHome: Boolean)
}
