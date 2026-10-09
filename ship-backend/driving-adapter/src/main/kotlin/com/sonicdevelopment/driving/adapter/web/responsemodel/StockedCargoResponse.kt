package com.sonicdevelopment.driving.adapter.web.responsemodel

import java.util.UUID

/**
 * A catalog Cargo with how many of it the Harbor's Stock holds, possibly 0, and its Price as a decimal
 * string with two decimals ("42.00"), `null` before the Harbor's first opening has rolled one.
 */
data class StockedCargoResponse(
    val cargoId: UUID,
    val name: String,
    val quantity: Int,
    val price: String?
)
