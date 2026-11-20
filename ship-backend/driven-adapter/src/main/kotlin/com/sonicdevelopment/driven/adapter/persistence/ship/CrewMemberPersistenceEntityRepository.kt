package com.sonicdevelopment.driven.adapter.persistence.ship

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CrewMemberPersistenceEntityRepository : JpaRepository<CrewMemberPersistenceEntity, Long> {
    fun findAllByShip_IdOrderById(id: Long): List<CrewMemberPersistenceEntity>
    fun deleteByCrewMemberIdAndShip_ShipId(crewMemberId: UUID, shipId: UUID): Long
    @Modifying(flushAutomatically = true)
    @Query("delete from CrewMemberPersistenceEntity c where c.ship.id = :shipId")
    fun deleteAllAboard(@Param("shipId") shipId: Long): Int

    fun deleteAllByShip_ShipId(shipId: UUID)
}
