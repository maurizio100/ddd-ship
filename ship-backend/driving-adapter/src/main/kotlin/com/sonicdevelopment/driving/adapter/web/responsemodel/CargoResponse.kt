package com.sonicdevelopment.driving.adapter.web.responsemodel

import java.util.UUID

data class CargoResponse(
    val id: UUID,
    val name: String,
    val weight: Float
) {
}
