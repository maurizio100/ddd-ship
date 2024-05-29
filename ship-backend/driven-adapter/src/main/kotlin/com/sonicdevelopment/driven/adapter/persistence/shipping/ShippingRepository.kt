package com.sonicdevelopment.driven.adapter.persistence.shipping

import org.springframework.data.jpa.repository.JpaRepository

interface ShippingRepository: JpaRepository<ShippingPersistenceEntity, Long> {
    fun deleteByShip_Id(shipId: Long)
}
