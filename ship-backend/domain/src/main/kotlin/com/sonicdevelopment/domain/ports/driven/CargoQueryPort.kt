package com.sonicdevelopment.domain.ports.driven

import com.sonicdevelopment.domain.model.Cargo

interface CargoQueryPort {
    fun findAvailableCargo(): List<Cargo>
    fun findCargo(cargoId: Long): Cargo?
}
