package com.sonicdevelopment.domain.ports.driven


interface ShipPersistencePort {
    fun saveNewShip(ship: InitialShipInformation): Long?
    fun delete(shipId: Long)

    class InitialShipInformation(
        val shipName: String
    )
}
