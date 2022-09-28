package at.willhaben.domain.ports.driven

import at.willhaben.domain.model.Ship

interface ShippingQueryPort {
    fun getShipForShippingId(shippingId: Long): Ship?
}
