package at.willhaben.driven.adapter.persistence

import javax.persistence.*

@Entity
@Table(name = "ships")
class ShipPersistenceEntity(
    @Id
    @GeneratedValue
    @Column(name = "id")
    var id: Long? = null,

    @Column(name = "ship_name")
    var shipName: String
) {
}
