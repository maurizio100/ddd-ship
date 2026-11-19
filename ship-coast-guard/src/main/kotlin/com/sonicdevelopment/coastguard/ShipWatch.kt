package com.sonicdevelopment.coastguard

import java.time.Duration
import java.util.UUID

val WINDOW: Duration = Duration.ofMinutes(10)
const val VOYAGES_TO_IMPRISON = 3

/** A voyage carrying Contraband the Coast Guard has seen; `spent` once it led to an imprisonment. */
data class CountedVoyage(val shippingId: UUID, val at: Long, val spent: Boolean)

/** One ship's recent voyages carrying Contraband, and the rule that imprisons it. */
data class ShipWatch(val voyages: List<CountedVoyage> = emptyList()) {

    /** Records a voyage released at [at] (epoch millis); returns the new watch and whether it imprisons the ship. */
    fun record(shippingId: UUID, at: Long): Pair<ShipWatch, Boolean> {
        if (voyages.any { it.shippingId == shippingId }) return Pair(this, false)
        val newest = maxOf(at, voyages.maxOfOrNull { it.at } ?: at)
        val windowStart = newest - WINDOW.toMillis()
        if (at < windowStart) return Pair(this, false)

        val recent = voyages.filter { it.at >= windowStart } + CountedVoyage(shippingId, at, spent = false)
        val counted = recent.count { !it.spent && withinWindow(it.at, at) }
        return if (counted >= VOYAGES_TO_IMPRISON) {
            Pair(ShipWatch(recent.map { it.copy(spent = true) }), true)
        } else {
            Pair(ShipWatch(recent), false)
        }
    }

    private fun withinWindow(voyageAt: Long, at: Long): Boolean =
        Math.abs(at - voyageAt) <= WINDOW.toMillis()
}
