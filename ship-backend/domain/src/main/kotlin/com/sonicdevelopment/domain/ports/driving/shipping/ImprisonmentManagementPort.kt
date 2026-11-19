package com.sonicdevelopment.domain.ports.driving.shipping

import com.sonicdevelopment.domain.model.values.EventId
import com.sonicdevelopment.domain.model.values.ShipId

interface ImprisonmentManagementPort {
    /** Handles the Ship Imprisoned event [eventId]: carries out the imprisonment, if this Harbor holds the ship. */
    fun receiveShipImprisoned(eventId: EventId, shipImprisoned: ShipImprisonedDTO)

    /**
     * Pays the Fine of an Imprisoned Ship at its Home Harbor from the Savings and frees it, in one transaction;
     * `null` for a ship that is not in the fleet. Refused whole, changing nothing, when the ship is not imprisoned
     * here or the Savings do not cover the Fine.
     */
    fun payFine(shipId: ShipId): FinePaymentDTO?
}
