package com.sonicdevelopment.domain.ports.driven

import com.sonicdevelopment.domain.model.Ship

interface ShipPersistencePort {
    fun saveNewShip(ship: Ship): Ship
    fun delete(shipId: Long)
    fun saveCargoLoad(ship: Ship): Ship
}
