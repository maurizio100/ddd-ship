package com.sonicdevelopment.driven.adapter.persistence.ship

import com.sonicdevelopment.driven.adapter.persistence.cargo.CargoPersistenceEntity
import jakarta.persistence.*

/** One Cargo instance aboard a ship that has no Shipping (an Incoming Ship). */
@Entity
@Table(name = "ships_cargos_aboard")
class ShipCargoAboardPersistenceEntity(
    @Id
    @GeneratedValue
    @Column(name = "id")
    var id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "ship_id")
    var ship: ShipPersistenceEntity,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cargo_id")
    var cargo: CargoPersistenceEntity,
)
