package com.sonicdevelopment.driven.adapter.persistence.outbox

import com.sonicdevelopment.driven.adapter.persistence.outbox.events.ShippingEvent
import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.util.*

@Entity
@Table(name ="shipping_outbox")
class ShippingOutboxPersistenceEntity(

    @Id
    @UuidGenerator
    @Column(name = "message_id")
    var id: UUID? = null,

    @Column(name = "aggregate_type")
    var aggregatetype: String,

    @Column(name = "aggregate_id")
    var aggregateId: UUID,

    @Column(name = "event_type")
    var type: String,

    @Column(name = "payload", columnDefinition = "TEXT")
    @Convert(converter = ShippingEventConverter::class)
    var payload: ShippingEvent,
) {
}