package com.sonicdevelopment.driven.adapter.persistence.cargo

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "cargos")
class CargoPersistenceEntity(
    @Id
    @GeneratedValue
    @Column(name = "id")
    var id: Long,

    @Column(name = "cargo_id")
    var cargoId: UUID,

    @Column(name = "cargo_name")
    var cargoName: String,

    @Column(name = "cargo_weight")
    var cargoWeight: Float
) {
}
