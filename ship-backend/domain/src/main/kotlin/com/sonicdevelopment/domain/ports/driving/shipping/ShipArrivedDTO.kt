package com.sonicdevelopment.domain.ports.driving.shipping

import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.model.values.ShippingId

/**
 * A Ship Arrived as the Destination Harbor announced it: the ship, the Shipping that brought it there,
 * and the Harbors it sailed from and to.
 */
data class ShipArrivedDTO(
    val shipId: ShipId,
    val shippingId: ShippingId,
    val originHarbor: HarborName?,
    val destinationHarbor: HarborName?,
)
