package com.sonicdevelopment.domain.model

import com.sonicdevelopment.domain.model.values.CargoId

class Cargo(
    val id: CargoId,
    val name: String,
    val weight: Float
) {
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        other as Cargo
        return other.name == name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}
