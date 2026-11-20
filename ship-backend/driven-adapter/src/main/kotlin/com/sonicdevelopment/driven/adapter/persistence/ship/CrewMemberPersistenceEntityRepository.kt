package com.sonicdevelopment.driven.adapter.persistence.ship

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CrewMemberPersistenceEntityRepository : JpaRepository<CrewMemberPersistenceEntity, Long> {
    fun findAllByShip_IdOrderById(id: Long): List<CrewMemberPersistenceEntity>
    fun deleteByCrewMemberIdAndShip_ShipId(crewMemberId: UUID, shipId: UUID): Long
    fun deleteAllByShip_ShipId(shipId: UUID)
}
