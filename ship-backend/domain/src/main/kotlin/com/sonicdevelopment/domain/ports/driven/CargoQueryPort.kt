package com.sonicdevelopment.domain.ports.driven

import com.sonicdevelopment.domain.model.Cargo
import com.sonicdevelopment.domain.model.values.CargoId

interface CargoQueryPort {
    /** The Cargo catalog: every Cargo there is, whether or not this Harbor has it in Stock. */
    fun findAllCargo(): List<Cargo>
    fun findCargo(cargoId: CargoId): Cargo?
}
