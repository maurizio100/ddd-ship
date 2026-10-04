package com.sonicdevelopment.driven.adapter.persistence.arrival

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface ArrivalPersistenceEntityRepository : JpaRepository<ArrivalPersistenceEntity, UUID> {

    /** Records the Arrival unless that Shipping has already arrived; returns the number of inserted rows (1 or 0). */
    @Modifying
    @Query(
        nativeQuery = true,
        value = "INSERT INTO arrivals (shipping_id, ship_id, arrived_at) VALUES (:shippingId, :shipId, now()) " +
            "ON CONFLICT (shipping_id) DO NOTHING"
    )
    fun insertIfAbsent(@Param("shippingId") shippingId: UUID, @Param("shipId") shipId: UUID): Int
}
