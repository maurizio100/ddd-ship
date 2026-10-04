package com.sonicdevelopment.domain.ports.driven

import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.ShippingId

interface ShippingOutboxRepository {
    fun broadcastShipping(ship: Ship, originHarbor: HarborName)

    /**
     * Writes Ship Arrived for the Shipping [shippingId] of [ship], addressed to [originHarbor], in the
     * caller's transaction, so it commits together with the Arrival it announces.
     */
    fun announceShipArrived(ship: Ship, shippingId: ShippingId, originHarbor: HarborName, destinationHarbor: HarborName)
}
