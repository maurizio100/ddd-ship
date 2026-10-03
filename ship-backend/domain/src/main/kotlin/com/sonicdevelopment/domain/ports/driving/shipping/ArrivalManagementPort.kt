package com.sonicdevelopment.domain.ports.driving.shipping

import com.sonicdevelopment.domain.model.values.EventId

interface ArrivalManagementPort {
    /** Handles the Shipping Published event [eventId]: the Arrival, if the ship sails to this Harbor. */
    fun receiveShippingPublished(eventId: EventId, shippingPublished: ShippingPublishedDTO)
}
