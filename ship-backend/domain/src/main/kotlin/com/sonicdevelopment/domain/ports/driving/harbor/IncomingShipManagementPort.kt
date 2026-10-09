package com.sonicdevelopment.domain.ports.driving.harbor

import com.sonicdevelopment.domain.model.values.HarborName
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

    /**
     * Refuses the Incoming Ship [shipId]: it sails back to its Home Harbor with every Cargo aboard as the
     * Loaded Cargo of a new Shipping. Nothing is paid and the Stock is unchanged. Returns the Home Harbor the
     * ship sails to, or `null` for a ship not in the fleet.
     */
    fun refuseIncomingShip(shipId: ShipId): HarborName?
}
