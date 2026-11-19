package com.sonicdevelopment.domain.ports.driving.shipping

import com.sonicdevelopment.domain.model.values.EventId

interface ImprisonmentManagementPort {
    /** Handles the Ship Imprisoned event [eventId]: carries out the imprisonment, if this Harbor holds the ship. */
    fun receiveShipImprisoned(eventId: EventId, shipImprisoned: ShipImprisonedDTO)
}
