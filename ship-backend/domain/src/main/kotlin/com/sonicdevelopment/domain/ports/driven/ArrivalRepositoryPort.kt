package com.sonicdevelopment.domain.ports.driven

import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.model.values.ShippingId

/**
 * The Arrivals this Harbor has handled, keyed by the Origin Harbor's Shipping id: makes sure one Release
 * arrives at most once, even when its Shipping Published is re-published under a new event id.
 */
interface ArrivalRepositoryPort {
    /**
     * Records that the ship [shipId] arrived on the Shipping [shippingId].
     *
     * Must run inside the caller's transaction, so the record commits or rolls back together with the
     * Arrival itself.
     *
     * @return `true` when the Arrival was newly recorded, `false` when that Shipping had already arrived.
     */
    fun recordArrival(shippingId: ShippingId, shipId: ShipId): Boolean
}
