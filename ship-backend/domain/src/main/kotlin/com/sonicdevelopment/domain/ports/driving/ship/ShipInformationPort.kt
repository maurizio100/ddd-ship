package com.sonicdevelopment.domain.ports.driving.ship

import com.sonicdevelopment.domain.model.values.ShipId

interface ShipInformationPort {
    fun getAllShips(): List<ShipDTO>
    fun getShipDetails(shipId: ShipId): ShipDetailDTO?
}
