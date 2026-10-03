package com.sonicdevelopment.driving.adapter.messaging.events

import java.util.*

/**
 * Inbound copy of the `ship-arrived` payload: only the fields the end of the voyage needs. The Ship Name
 * is left out and ignored.
 */
data class ShipArrivedInboundEvent(
    val shipId: UUID,
    val shippingId: UUID,
    val originHarbor: String? = null,
    val destinationHarbor: String? = null,
)
