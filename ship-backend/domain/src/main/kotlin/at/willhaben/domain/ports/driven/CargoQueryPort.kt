package at.willhaben.domain.ports.driven

import at.willhaben.domain.model.Cargo

interface CargoQueryPort {
    fun findAvailableCargo(): List<Cargo>
    fun findCargo(cargoId: Long): Cargo?
}
