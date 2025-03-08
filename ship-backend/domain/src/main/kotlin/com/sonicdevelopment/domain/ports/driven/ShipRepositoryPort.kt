package com.sonicdevelopment.domain.ports.driven

import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.values.CatainId
import com.sonicdevelopment.domain.model.values.ShipId

interface ShipRepositoryPort {
    fun saveNewShip(ship: InitialShipInformation): Long?

    class InitialShipInformation private constructor(
        val shipId: ShipId,
        val shipName: String,
        val catainId: CatainId
    ) {
        companion object {
            fun fromShip(ship: Ship) = InitialShipInformation(ship.id, shipName = ship.shipName, ship.catainId)
        }
    }
    fun getAllShips(): List<Ship>
    fun getShipDetails(shipId: ShipId): Ship?
    fun delete(shipId: ShipId)
}
