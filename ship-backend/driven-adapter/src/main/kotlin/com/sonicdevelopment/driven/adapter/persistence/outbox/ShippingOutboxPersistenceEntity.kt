package com.sonicdevelopment.driven.adapter.persistence.outbox

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import org.hibernate.annotations.UuidGenerator.*
import java.util.*

@Entity
@Table(name ="shipping_outbox")
class ShippingOutboxPersistenceEntity(

    @Id
    @UuidGenerator(style = Style.RANDOM)
    @Column(name = "message_id")
    var id: UUID? = null,

    @Column(name = "aggregate_type")
    var aggregatetype: String,

    @Column(name = "aggregate_id")
    var aggregateId: UUID,

    @Column(name = "event_type")
    var type: String,

    /** JSON of a `<Name>Event` from `outbox/events`, serialized by the outbox adapter that writes the row. */
    @Column(name = "payload", columnDefinition = "TEXT")
    var payload: String
)
