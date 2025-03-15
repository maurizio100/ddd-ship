package com.sonicdevelopment.domain.ports.driving.shipping

import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.ports.driving.ship.ShipDetailDTO


interface ShippingManagementPort {
    fun createShipping(shipId: ShipId): ShipDetailDTO?
    fun releaseShipping(shipId: ShipId): ShipDetailDTO?
}
