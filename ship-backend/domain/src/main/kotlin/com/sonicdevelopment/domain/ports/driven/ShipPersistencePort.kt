package com.sonicdevelopment.domain.ports.driven

import com.sonicdevelopment.domain.model.Ship

interface ShipPersistencePort {
    fun save(ship: Ship): Ship
    fun delete(shipId: Long)
}
