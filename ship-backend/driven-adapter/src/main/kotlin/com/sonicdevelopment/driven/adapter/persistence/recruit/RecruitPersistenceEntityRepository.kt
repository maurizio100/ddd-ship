package com.sonicdevelopment.driven.adapter.persistence.recruit

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface RecruitPersistenceEntityRepository : JpaRepository<RecruitPersistenceEntity, Long> {

    fun findAllByRecruitHiredFalseOrderByOrdinalAsc(): List<RecruitPersistenceEntity>

    @Query("SELECT max(r.ordinal) FROM RecruitPersistenceEntity r")
    fun findMaxOrdinal(): Int?

    /** Marks an offered Recruit hired; returns the number of updated rows (1 or 0), so a Recruit is Hired once. */
    @Modifying
    @Query(
        nativeQuery = true,
        value = "UPDATE recruits SET recruit_hired = true WHERE recruit_id = :recruitId AND NOT recruit_hired"
    )
    fun hire(@Param("recruitId") recruitId: UUID): Int

    /** Stores the Recruit under [ordinal] unless it is taken; returns the number of inserted rows (1 or 0). */
    @Modifying
    @Query(
        nativeQuery = true,
        value = "INSERT INTO recruits (id, recruit_id, ordinal, name, trait) " +
            "VALUES (nextval('recruits_seq'), :recruitId, :ordinal, :name, :trait) " +
            "ON CONFLICT (ordinal) DO NOTHING"
    )
    fun remember(
        @Param("recruitId") recruitId: UUID,
        @Param("ordinal") ordinal: Int,
        @Param("name") name: String,
        @Param("trait") trait: String
    ): Int
}
