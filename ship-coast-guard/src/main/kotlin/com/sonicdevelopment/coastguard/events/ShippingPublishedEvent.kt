package com.sonicdevelopment.coastguard.events

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.UUID

const val SHIPPING_PUBLISHED = "shipping-published"

/** The Coast Guard's own copy of Shipping Published, holding only the fields it reads. */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ShippingPublishedEvent(
    val shipEventData: ShipEventData,
    val shippingEventData: ShippingEventData,
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ShipEventData(val shipId: UUID, val shipName: String, val homeHarbor: String)

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ShippingEventData(
        val shippingId: UUID,
        val destinationHarbor: String,
        val cargo: List<CargoEventData> = emptyList(),
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class CargoEventData(val cargoName: String)
}
