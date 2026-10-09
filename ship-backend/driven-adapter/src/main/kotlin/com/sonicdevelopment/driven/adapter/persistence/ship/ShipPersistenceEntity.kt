package com.sonicdevelopment.driven.adapter.persistence.ship

import com.sonicdevelopment.driven.adapter.persistence.catain.CatainPersistenceEntity
import jakarta.persistence.*
import java.math.BigDecimal
import java.util.*

@Entity
@Table(name = "ships")
class ShipPersistenceEntity(
    @Id
    @GeneratedValue
    @Column(name = "id")
    var id: Long? = null,

    @Column(name = "ship_id")
    var shipId: UUID,

    @Column(name = "ship_name")
    var shipName: String,

    @ManyToOne(fetch = FetchType.EAGER)
    var catain: CatainPersistenceEntity,

    /** Whether the ship is in this Harbor's fleet; a ship that sailed to another Harbor keeps its row. */
    @Column(name = "ship_in_fleet")
    var inFleet: Boolean = true,

    /** The Origin Harbor of the Arrival that last took the ship into this fleet; `null` for a ship registered here. */
    @Column(name = "ship_arrived_from")
    var arrivedFrom: String? = null,

    /** The Harbor where the ship was registered; never changes. */
    @Column(name = "ship_home_harbor")
    var homeHarbor: String,

    /** Whether the ship is an Incoming Ship; its Cargo aboard is in `ships_cargos_aboard`. */
    @Column(name = "ship_incoming")
    var incoming: Boolean = false,

    /** The Earnings the ship carries until it reaches its Home Harbor; only grows through an atomic update. */
    @Column(name = "ship_earnings")
    var earnings: BigDecimal = BigDecimal.ZERO,
) {
}
