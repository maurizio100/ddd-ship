package com.sonicdevelopment.domain.ports.driven

import com.sonicdevelopment.domain.model.Ship


interface ShippingPersistencePort {
    fun createShipping(ship: Ship): Ship
}
