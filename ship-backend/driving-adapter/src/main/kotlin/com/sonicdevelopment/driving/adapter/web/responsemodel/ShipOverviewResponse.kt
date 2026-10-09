package com.sonicdevelopment.driving.adapter.web.responsemodel

import com.sonicdevelopment.domain.model.enums.ShippingState
import java.util.*

data class ShipOverviewResponse(
    val id: UUID,
    val name: String,
    val catain: String,
    val shippingState: ShippingState?,
    val arrivedFrom: String? = null,
    /** Whether the ship is an Incoming Ship, which cannot start a new Shipping until it is unloaded or refused. */
    val incoming: Boolean = false,
    /** The Harbor where the ship was registered; never changes. */
    val homeHarbor: String,
)
