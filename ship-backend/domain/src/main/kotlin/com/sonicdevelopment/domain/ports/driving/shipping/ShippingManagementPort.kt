package com.sonicdevelopment.domain.ports.driving.shipping

import com.sonicdevelopment.domain.model.values.ShipId

interface ShippingManagementPort {
    fun createShipping(shipId: ShipId): ShippingDetailsDTO?
    fun releaseShipping(shipId: ShipId): ShippingDetailsDTO?
}
