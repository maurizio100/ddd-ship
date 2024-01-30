package com.sonicdevelopment.domain.ports.driven

import com.sonicdevelopment.domain.model.Ship

interface ShipQueryPort {
    fun getAllShips(): List<Ship>
    fun getShipDetails(shipId: Long): Ship?
}
