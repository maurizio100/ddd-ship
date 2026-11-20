package com.sonicdevelopment.driving.adapter.web.responsemodel

import com.sonicdevelopment.domain.model.enums.ShipClass

/** A Ship Class offered when a ship is created, with the Crew Capacity a ship of that class gets. */
data class ShipClassOfferResponse(
    val shipClass: ShipClass,
    val crewCapacity: Int,
)
