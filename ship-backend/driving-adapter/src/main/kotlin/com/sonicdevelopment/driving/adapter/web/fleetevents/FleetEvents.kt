package com.sonicdevelopment.driving.adapter.web.fleetevents

import java.util.UUID

/** Data of the `ship-arrived` fleet event: a ship arrived at this Harbor from [originHarbor]. */
data class ShipArrivedFleetEvent(
    val shipId: UUID,
    val shipName: String,
    val originHarbor: String,
)

/** Data of the `ship-left` fleet event: a ship arrived at [destinationHarbor] and left this Harbor's fleet. */
data class ShipLeftFleetEvent(
    val shipId: UUID,
    val shipName: String,
    val destinationHarbor: String?,
)
