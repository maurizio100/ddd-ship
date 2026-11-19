package com.sonicdevelopment.coastguard

import java.time.Duration
import java.util.UUID

val WINDOW: Duration = Duration.ofMinutes(10)
const val VOYAGES_TO_IMPRISON = 3

data class CountedVoyage(val shippingId: UUID, val at: Long, val spent: Boolean)

data class ShipWatch(val voyages: List<CountedVoyage> = emptyList()) {
    fun record(shippingId: UUID, at: Long): Pair<ShipWatch, Boolean> = Pair(this, false)
}
