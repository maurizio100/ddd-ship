package com.sonicdevelopment.domain.ports.driven

import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.model.values.ShippingId
import com.sonicdevelopment.domain.ports.driving.shipping.ShippingDetailsDTO


interface ShippingRepositoryPort {
    fun createShipping(ship: Ship)
    fun updateActiveShipping(ship: Ship)
    fun getShippingInformation(shipId: ShipId, shippingId: ShippingId): ShippingDetailsDTO?
}
