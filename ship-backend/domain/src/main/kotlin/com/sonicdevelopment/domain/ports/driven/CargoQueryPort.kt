package com.sonicdevelopment.domain.ports.driven

import com.sonicdevelopment.domain.model.Cargo
import com.sonicdevelopment.domain.model.values.CargoId

interface CargoQueryPort {
    fun findAvailableCargo(): List<Cargo>
    fun findCargo(cargoId: CargoId): Cargo?
}
