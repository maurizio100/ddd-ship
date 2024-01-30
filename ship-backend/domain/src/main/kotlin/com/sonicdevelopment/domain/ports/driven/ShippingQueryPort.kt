package com.sonicdevelopment.domain.ports.driven

import com.sonicdevelopment.domain.model.Ship

interface ShippingQueryPort {
    fun getShipForShippingId(shippingId: Long): Ship?
}
