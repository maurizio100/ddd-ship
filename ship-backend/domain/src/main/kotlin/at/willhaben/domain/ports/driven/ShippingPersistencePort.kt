package at.willhaben.domain.ports.driven

import at.willhaben.domain.model.Ship
import at.willhaben.domain.model.Shipping

interface ShippingPersistencePort {
    fun createShipping(ship: Ship): Ship
}
