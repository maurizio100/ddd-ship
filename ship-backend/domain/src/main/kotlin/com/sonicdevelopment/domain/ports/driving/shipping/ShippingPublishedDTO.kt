package com.sonicdevelopment.domain.ports.driving.shipping

import com.sonicdevelopment.domain.model.values.CargoId
import com.sonicdevelopment.domain.model.values.CatainId
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.model.values.ShippingId

/**
 * A Shipping Published as another Harbor (or this one) Released it: the ship, its Catain, its Loaded
 * Cargo and where it sails from and to. Both Harbors are `null` for events published before STORY-005.
 */
data class ShippingPublishedDTO(
    val shipId: ShipId,
    val shipName: String,
    val catainId: CatainId,
    val shippingId: ShippingId,
    val cargoIds: List<CargoId>,
    val originHarbor: HarborName?,
    val destinationHarbor: HarborName?,
)
