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

fun eventIdOf(shippingId: UUID): UUID = UUID(0, 0)
