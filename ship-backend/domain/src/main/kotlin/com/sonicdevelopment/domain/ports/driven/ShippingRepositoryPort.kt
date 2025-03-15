package com.sonicdevelopment.domain.ports.driven

import com.sonicdevelopment.domain.model.Ship


interface ShippingRepositoryPort {
    fun createShipping(ship: Ship): Ship
    fun updateActiveShipping(ship: Ship)
}
