package com.sonicdevelopment.driving.adapter.web.responsemodel

import java.util.*

data class ShipDetailResponse(
    val id: UUID,
    val name: String,
    val cargo: List<CargoResponse> = listOf(),
    /** Cargo refused by this Harbor, the ship's Home Harbor; it cannot be unloaded while preparing and sails on Release. */
    val cargoAboard: List<CargoResponse> = listOf(),
    val weight: Float = 0.0F,
    val maxweight: Float = 0.0F,
    val arrivedFrom: String? = null,
    /** The Harbor where the ship was registered; never changes. */
    val homeHarbor: String,
)
