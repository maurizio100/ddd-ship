package com.sonicdevelopment.driving.adapter.web.responsemodel

import com.sonicdevelopment.domain.model.enums.ShippingState
import java.util.*

data class ShipOverviewResponse(
    val id: UUID,
    val name: String,
    val shippingState: ShippingState?
)
