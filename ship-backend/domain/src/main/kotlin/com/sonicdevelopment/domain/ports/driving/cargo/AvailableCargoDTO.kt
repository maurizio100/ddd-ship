package com.sonicdevelopment.domain.ports.driving.cargo

import com.sonicdevelopment.domain.model.values.CargoId

/** A Cargo that can be loaded at this Harbor, with how many of it the Stock holds. */
data class AvailableCargoDTO(
    val id: CargoId,
    val name: String,
    val weight: Float,
    val stock: Int
)
