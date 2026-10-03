package com.sonicdevelopment.driven.adapter.persistence.inbox

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.*

@Entity
@Table(name = "inbox_events")
class InboxEventPersistenceEntity(

    @Id
    @Column(name = "event_id")
    var eventId: UUID,

    @Column(name = "consumed_at")
    var consumedAt: Instant
)
