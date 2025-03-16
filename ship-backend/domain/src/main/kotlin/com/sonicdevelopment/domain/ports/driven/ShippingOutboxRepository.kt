package com.sonicdevelopment.domain.ports.driven

import com.sonicdevelopment.domain.model.Ship

interface ShippingOutboxRepository {
    fun broadcastShipping(ship: Ship)
}