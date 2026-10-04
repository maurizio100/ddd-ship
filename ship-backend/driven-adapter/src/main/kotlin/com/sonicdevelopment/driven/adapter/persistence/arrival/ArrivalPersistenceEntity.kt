package com.sonicdevelopment.driven.adapter.persistence.arrival

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.*

/** An Arrival this Harbor has handled, keyed by the Origin Harbor's Shipping id. */
@Entity
@Table(name = "arrivals")
class ArrivalPersistenceEntity(

    @Id
    @Column(name = "shipping_id")
    var shippingId: UUID,

    @Column(name = "ship_id")
    var shipId: UUID,

    @Column(name = "arrived_at")
    var arrivedAt: Instant
)
