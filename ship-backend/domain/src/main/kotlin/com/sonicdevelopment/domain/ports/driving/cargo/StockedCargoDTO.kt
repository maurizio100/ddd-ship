package com.sonicdevelopment.domain.ports.driving.cargo

import com.sonicdevelopment.domain.model.values.CargoId

/** A catalog Cargo with how many of it this Harbor's Stock holds, possibly 0. */
data class StockedCargoDTO(
    val id: CargoId,
    val name: String,
    val quantity: Int
)
