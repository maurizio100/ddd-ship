package com.sonicdevelopment.domain.ports.driving.harbor

import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.domain.model.values.ShipId

/**
 * An Imprisoned Ship at this, its Home, Harbor as the Imprisoned Ships page lists it: its [fine], and whether this
 * Harbor's Savings cover it.
 */
data class ImprisonedShipDTO(
    val id: ShipId,
    val name: String,
    val catain: String,
    val homeHarbor: HarborName,
    val fine: Money,
    val savingsCoverFine: Boolean,
)
