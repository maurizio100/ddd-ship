package com.sonicdevelopment.driven.adapter.persistence.shipping

import com.sonicdevelopment.driven.adapter.persistence.ship.ShipPersistenceEntity
import jakarta.persistence.*

@Entity
@Table(name = "shippings")
class ShippingPersistenceEntity(
    @Id
    @GeneratedValue
    @Column(name = "id")
    var id: Long? = null,

    @Column(name = "sailors_code")
    var sailorsCode: String,

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="ship_id", nullable=false)
    var ship: ShipPersistenceEntity
)

