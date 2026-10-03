package com.sonicdevelopment.driven.adapter.persistence.inbox

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface InboxEventPersistenceEntityRepository : JpaRepository<InboxEventPersistenceEntity, UUID> {

    /** Records [eventId] unless it is already there; returns the number of inserted rows (1 or 0). */
    @Modifying
    @Query(
        nativeQuery = true,
        value = "INSERT INTO inbox_events (event_id, consumed_at) VALUES (:eventId, now()) ON CONFLICT (event_id) DO NOTHING"
    )
    fun insertIfAbsent(@Param("eventId") eventId: UUID): Int
}
