package com.sonicdevelopment.domain.ports.driving.harbor

import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.domain.model.values.ShipId

/** What the User does with an Incoming Ship of this Harbor. */
interface IncomingShipManagementPort {
    /**
     * Unloads the Incoming Ship [shipId]: pays its Delivery Price from the Savings, puts every Cargo aboard
     * into the Stock, and the ship is no longer Incoming. Returns the Delivery Price paid, or `null` for a
     * ship not in the fleet.
     */
    fun unloadIncomingShip(shipId: ShipId): Money?
}
