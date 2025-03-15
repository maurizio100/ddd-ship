package com.sonicdevelopment.domain.ports.driving.cargo

import com.sonicdevelopment.domain.model.values.CargoId

data class CargoDTO(
    val id: CargoId,
    val name: String,
    val weight: Float
)
