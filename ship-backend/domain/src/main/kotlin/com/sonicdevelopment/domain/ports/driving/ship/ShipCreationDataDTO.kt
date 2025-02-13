package com.sonicdevelopment.domain.ports.driving.ship

import com.sonicdevelopment.domain.model.values.CatainId

data class ShipCreationDataDTO(
    val name: String? = null,
    val catainId: CatainId
)
