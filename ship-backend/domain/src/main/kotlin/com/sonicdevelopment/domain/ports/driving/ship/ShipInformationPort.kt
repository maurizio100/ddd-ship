package com.sonicdevelopment.domain.ports.driving.ship

interface ShipInformationPort {
    fun getAllShips(): List<ShipDTO>
    fun getShipDetails(shipId: Long): ShipDetailDTO?
}
