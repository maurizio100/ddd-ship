package com.sonicdevelopment.driven.adapter.persistence.ship

import com.sonicdevelopment.driven.adapter.persistence.catain.CatainPersistenceEntity
import jakarta.persistence.*
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

    @ManyToOne
    var catain: CatainPersistenceEntity
) {
}
