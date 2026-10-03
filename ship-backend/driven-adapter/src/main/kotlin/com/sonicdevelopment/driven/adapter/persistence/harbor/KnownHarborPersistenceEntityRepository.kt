package com.sonicdevelopment.driven.adapter.persistence.harbor

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface KnownHarborPersistenceEntityRepository : JpaRepository<KnownHarborPersistenceEntity, Long> {

    /** Stores [harborName] unless it is already known; returns the number of inserted rows (1 or 0). */
    @Modifying
    @Query(
        nativeQuery = true,
        value = "INSERT INTO known_harbors (id, harbor_name) VALUES (nextval('known_harbors_seq'), :harborName) " +
            "ON CONFLICT (harbor_name) DO NOTHING"
    )
    fun insertIfAbsent(@Param("harborName") harborName: String): Int

    fun findAllByOrderByHarborNameAsc(): List<KnownHarborPersistenceEntity>
}
