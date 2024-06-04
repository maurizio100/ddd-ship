package com.sonicdevelopment.domain.ports.driven

import com.sonicdevelopment.domain.model.Ship


interface ShipPersistencePort {
    fun saveNewShip(ship: InitialShipInformation): Long?
    fun delete(shipId: Long)

    class InitialShipInformation private constructor(
        val shipName: String
    ) {
        companion object {
            fun fromShip(ship: Ship) = InitialShipInformation(shipName = ship.shipName)
        }
    }
}
