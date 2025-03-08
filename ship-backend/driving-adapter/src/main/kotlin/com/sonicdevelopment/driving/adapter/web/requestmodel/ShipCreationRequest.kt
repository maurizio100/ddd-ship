package com.sonicdevelopment.driving.adapter.web.requestmodel

import java.util.*

data class ShipCreationRequest(
    val name: String? = null,
    val catainId: UUID? = UUID.randomUUID()
)
