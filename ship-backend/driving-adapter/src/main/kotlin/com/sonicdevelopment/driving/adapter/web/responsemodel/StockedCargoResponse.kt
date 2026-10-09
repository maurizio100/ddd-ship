package com.sonicdevelopment.driving.adapter.web.responsemodel

import java.util.UUID

/** A catalog Cargo with how many of it the Harbor's Stock holds, possibly 0. */
data class StockedCargoResponse(
    val cargoId: UUID,
    val name: String,
    val quantity: Int
)
