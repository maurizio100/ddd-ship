package com.sonicdevelopment.domain.ports.driving.shipping

import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.ShipId

/** A Ship Imprisoned as the Coast Guard published it: the ship, the Harbor holding it, and its Home Harbor. */
data class ShipImprisonedDTO(
    val shipId: ShipId,
    val shipName: String,
    val holdingHarbor: HarborName,
    val homeHarbor: HarborName,
)
