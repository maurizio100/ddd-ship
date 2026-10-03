package com.sonicdevelopment.domain.ports.driven

import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.values.HarborName

interface ShippingOutboxRepository {
    fun broadcastShipping(ship: Ship, originHarbor: HarborName)
}