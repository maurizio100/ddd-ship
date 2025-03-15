package com.sonicdevelopment.driven.adapter.persistence.shipping

import com.sonicdevelopment.driven.adapter.persistence.cargo.CargoPersistenceEntity
import com.sonicdevelopment.driven.adapter.persistence.ship.ShipPersistenceEntity
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "shippings")
class ShippingPersistenceEntity(
    @Id
    @GeneratedValue
    @Column(name = "id")
    var id: Long? = null,

    @Column(name = "shipping_id")
    var shippingId: UUID,

    @Column(name = "sailors_code")
    var sailorsCode: String?,

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="ship_id", nullable=false)
    var ship: ShipPersistenceEntity,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "ships_cargos",
        joinColumns =
        [JoinColumn(name = "ship_id", referencedColumnName = "id")],
        inverseJoinColumns =
        [JoinColumn(name = "cargo_id", referencedColumnName = "id")]
    )
    var cargoLoad: MutableList<CargoPersistenceEntity> = mutableListOf(),

    @Column(name = "shipping_state")
    @Enumerated(EnumType.STRING)
    var shppingState: ShippingStateEnumEntity
)

