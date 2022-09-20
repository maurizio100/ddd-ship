package at.willhaben.domain.ports.driven

import at.willhaben.domain.model.Ship

interface ShipQueryPort {
    fun getAllShips(): List<Ship>
    fun getShipDetails(shipId: Long): Ship
}
