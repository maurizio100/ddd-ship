package com.sonicdevelopment.domain.ports.driving.shipping

import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.model.values.ShippingId
import com.sonicdevelopment.domain.ports.driving.ship.ShipDetailDTO

interface ShippingInformationPort {
    fun getAllShipping(shipId: ShipId): List<ShipDetailDTO>
    fun getShipping(shipId: ShipId, shippingId: ShippingId): ShipDetailDTO?

}
