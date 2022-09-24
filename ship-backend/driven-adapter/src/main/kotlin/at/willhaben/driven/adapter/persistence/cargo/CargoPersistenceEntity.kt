package at.willhaben.driven.adapter.persistence.cargo

import javax.persistence.*

@Entity
@Table(name = "cargos")
class CargoPersistenceEntity(
    @Id
    @GeneratedValue
    @Column(name = "id")
    var id: Long ,

    @Column(name = "cargo_name")
    var cargoName: String,

    @Column(name = "cargo_weight")
    var cargoWeight: Float
) {
}
