package com.sonicdevelopment.coastguard.events

import java.util.UUID

const val SHIP_IMPRISONED = "ship-imprisoned"

data class ShipImprisonedEvent(
    val shipId: UUID,
    val shipName: String,
    val holdingHarbor: String,
    val homeHarbor: String,
    val shippingId: UUID,
)

/** Derived from the tipping Shipping id, so a republished imprisonment carries the same event id. */
fun eventIdOf(shippingId: UUID): UUID = UUID.nameUUIDFromBytes("ship-imprisoned:$shippingId".toByteArray())
