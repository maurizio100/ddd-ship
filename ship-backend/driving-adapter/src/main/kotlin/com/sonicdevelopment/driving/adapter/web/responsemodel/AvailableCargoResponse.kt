package com.sonicdevelopment.driving.adapter.web.responsemodel

import java.util.UUID

/** A Cargo that can be loaded at this Harbor, with how many of it the Stock holds. */
data class AvailableCargoResponse(
    val id: UUID,
    val name: String,
    val weight: Float,
    val stock: Int
)
