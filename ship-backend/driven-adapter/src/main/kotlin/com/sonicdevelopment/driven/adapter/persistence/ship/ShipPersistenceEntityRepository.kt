package com.sonicdevelopment.driven.adapter.persistence.ship

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ShipPersistenceEntityRepository: JpaRepository<ShipPersistenceEntity, Long> {
    fun deleteByShipId(shipId: UUID)
    fun findByShipId(shipId: UUID): ShipPersistenceEntity?
}
