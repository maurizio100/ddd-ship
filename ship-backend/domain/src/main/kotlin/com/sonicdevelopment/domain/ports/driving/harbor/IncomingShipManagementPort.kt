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

    /**
     * Refuses the Incoming Ship [shipId]. Away from its Home Harbor it sails back there with every Cargo aboard
     * as the Loaded Cargo of a new Shipping. At its Home Harbor it stays in the fleet with its Cargo aboard,
     * no longer Incoming, and that Cargo can only be delivered to another Harbor. Nothing is paid and the Stock
     * is unchanged. Returns the outcome ([RefusalDTO.sailsTo]), or `null` for a ship not in the fleet.
     */
    fun refuseIncomingShip(shipId: ShipId): RefusalDTO?
}
