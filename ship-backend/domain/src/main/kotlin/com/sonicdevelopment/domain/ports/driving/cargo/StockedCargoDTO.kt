package com.sonicdevelopment.domain.ports.driving.cargo

import com.sonicdevelopment.domain.model.values.CargoId
import com.sonicdevelopment.domain.model.values.Money

/**
 * A catalog Cargo with how many of it this Harbor's Stock holds, possibly 0, and its Price at this Harbor;
 * [price] is `null` only before the Harbor's first opening has rolled one.
 */
data class StockedCargoDTO(
    val id: CargoId,
    val name: String,
    val quantity: Int,
    val price: Money?
)
