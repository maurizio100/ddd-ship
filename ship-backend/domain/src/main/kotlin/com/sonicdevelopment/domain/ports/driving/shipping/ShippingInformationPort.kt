package com.sonicdevelopment.domain.ports.driving.shipping

import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.model.values.ShippingId

interface ShippingInformationPort {
    fun getShipping(shipId: ShipId, shippingId: ShippingId): ShippingDetailsDTO?

}
