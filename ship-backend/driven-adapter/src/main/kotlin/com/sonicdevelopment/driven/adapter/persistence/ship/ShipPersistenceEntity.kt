package com.sonicdevelopment.driven.adapter.persistence.ship

import com.sonicdevelopment.driven.adapter.persistence.cargo.CargoPersistenceEntity
import com.sonicdevelopment.driven.adapter.persistence.catain.CatainPersistenceEntity
import com.sonicdevelopment.driven.adapter.persistence.shipping.ShippingPersistenceEntity
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "ships")
class ShipPersistenceEntity(
    @Id
    @GeneratedValue
    @Column(name = "id")
    var id: Long? = null,

    var shipId: UUID,

    @Column(name = "ship_name")
    var shipName: String,

    @ManyToOne
    var catain: CatainPersistenceEntity?,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "ships_cargos",
        joinColumns =
        [JoinColumn(name = "ship_id", referencedColumnName = "id")],
        inverseJoinColumns =
        [JoinColumn(name = "cargo_id", referencedColumnName = "id")]
    )
    var cargoLoad: MutableList<CargoPersistenceEntity> = mutableListOf(),

    @OneToOne
    @JoinColumn(name="shipping_id", nullable=true)
    var shipping: ShippingPersistenceEntity? = null
) {
}
