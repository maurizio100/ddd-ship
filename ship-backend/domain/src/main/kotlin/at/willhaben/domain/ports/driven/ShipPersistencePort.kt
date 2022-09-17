package at.willhaben.domain.ports.driven

import at.willhaben.domain.model.Ship

interface ShipPersistencePort {
    fun save(ship: Ship): Ship
}
