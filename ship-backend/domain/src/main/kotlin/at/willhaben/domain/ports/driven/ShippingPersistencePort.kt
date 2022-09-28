package at.willhaben.domain.ports.driven

import at.willhaben.domain.model.Ship


interface ShippingPersistencePort {
    fun createShipping(ship: Ship): Ship
}
