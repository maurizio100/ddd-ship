package com.sonicdevelopment.driven.adapter.persistence.shipping

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ShippingRepository: JpaRepository<ShippingPersistenceEntity, Long> {
    fun findByShippingId(shippingId: UUID): ShippingPersistenceEntity?
    fun deleteByShip_shipId(shipId: UUID)
    fun findByShip_ShipIdAndShppingStateIn(shipId: UUID, shippingState: List<ShippingStateEnumEntity>): ShippingPersistenceEntity?
}
