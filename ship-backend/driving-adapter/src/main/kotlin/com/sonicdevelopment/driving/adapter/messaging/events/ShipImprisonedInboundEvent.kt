package com.sonicdevelopment.driving.adapter.messaging.events

import java.util.*

/**
 * Inbound copy of the Coast Guard's `ship-imprisoned` payload: the ship, the Harbor holding it and its Home
 * Harbor. [shippingId] is the Shipping the Coast Guard saw; the imprisonment does not use it. Unknown fields
 * are ignored.
 */
data class ShipImprisonedInboundEvent(
    val shipId: UUID,
    val shipName: String,
    val holdingHarbor: String,
    val homeHarbor: String,
    val shippingId: UUID? = null,
)
