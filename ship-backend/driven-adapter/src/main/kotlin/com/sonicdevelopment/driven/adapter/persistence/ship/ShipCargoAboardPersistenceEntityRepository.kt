package com.sonicdevelopment.driven.adapter.persistence.ship

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ShipCargoAboardPersistenceEntityRepository : JpaRepository<ShipCargoAboardPersistenceEntity, Long> {
    fun findAllByShip_IdOrderById(id: Long): List<ShipCargoAboardPersistenceEntity>
    fun deleteAllByShip_Id(id: Long)
    fun deleteAllByShip_ShipId(shipId: UUID)
}
