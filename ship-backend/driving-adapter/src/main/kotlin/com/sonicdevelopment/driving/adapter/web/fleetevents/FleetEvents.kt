package com.sonicdevelopment.driving.adapter.web.fleetevents

import java.util.UUID

/**
 * Data of the `ship-arrived` fleet event: a ship arrived at this Harbor from [originHarbor]; [imprisoned] when it
 * arrived at its Home Harbor as an Imprisoned Ship.
 */
data class ShipArrivedFleetEvent(
    val shipId: UUID,
    val shipName: String,
    val originHarbor: String,
    val imprisoned: Boolean,
)

/**
 * Data of the `ship-imprisoned` fleet event: the Coast Guard imprisoned a ship here and its Cargo is confiscated;
 * it [sailsHome] to [homeHarbor], or, at its Home Harbor, stays here imprisoned.
 */
data class ShipImprisonedFleetEvent(
    val shipId: UUID,
    val shipName: String,
    val homeHarbor: String,
    val sailsHome: Boolean,
)

/** Data of the `ship-left` fleet event: a ship arrived at [destinationHarbor] and left this Harbor's fleet. */
data class ShipLeftFleetEvent(
    val shipId: UUID,
    val shipName: String,
    val destinationHarbor: String?,
)
