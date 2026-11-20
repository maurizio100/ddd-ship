package com.sonicdevelopment.driving.adapter.web.responsemodel

import com.sonicdevelopment.domain.model.enums.ShipClass
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
    /** The Earnings the ship carries until it reaches its Home Harbor: a decimal string with two decimals, `"0.00"` for none. */
    val earnings: String,
    /** Whether the ship is an Imprisoned Ship, which cannot start a new Shipping; also true while it sails home as one. */
    val imprisoned: Boolean = false,
    /** The ship's Ship Class: `SLOOP`, `BRIG` or `GALLEON`. */
    val shipClass: ShipClass,
    /** How many Crew Members the ship can carry, set by its Ship Class. */
    val crewCapacity: Int,
)
