package at.willhaben.driven.adapter.persistence.ship

import at.willhaben.driven.adapter.persistence.cargo.CargoPersistenceEntity
import javax.persistence.*

@Entity
@Table(name = "ships")
class ShipPersistenceEntity(
    @Id
    @GeneratedValue
    @Column(name = "id")
    var id: Long? = null,

    @Column(name = "ship_name")
    var shipName: String,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "ships_cargos",
        joinColumns =
        [JoinColumn(name = "ship_id", referencedColumnName = "id")],
        inverseJoinColumns =
        [JoinColumn(name = "cargo_id", referencedColumnName = "id")]
    )
    var cargoLoad: MutableList<CargoPersistenceEntity> = mutableListOf()
) {
}
