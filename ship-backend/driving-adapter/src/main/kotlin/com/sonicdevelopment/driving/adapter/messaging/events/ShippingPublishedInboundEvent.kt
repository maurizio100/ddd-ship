package com.sonicdevelopment.driving.adapter.messaging.events

import java.util.*

/**
 * Inbound copy of the `shipping-published` payload: only the fields the Arrival needs. Weight, Shipping
 * Quote and the names of Cargo and Catain are left out and ignored.
 */
data class ShippingPublishedInboundEvent(
    val shipEventData: ShipEventData,
    val catain: CatainEventData,
    val shippingEventData: ShippingEventData,
) {
    /**
     * [homeHarbor] and [earnings] are missing in events from Harbors that do not send them yet; [earnings] is
     * a decimal string with two decimals.
     */
    data class ShipEventData(
        val shipId: UUID,
        val shipName: String,
        val homeHarbor: String? = null,
        val earnings: String? = null,
    )

    data class CatainEventData(val catainId: UUID)

    data class ShippingEventData(
        val shippingId: UUID,
        val cargo: List<CargoEventData> = emptyList(),
        val originHarbor: String? = null,
        val destinationHarbor: String? = null,
    ) {
        data class CargoEventData(val cargoId: UUID)
    }
}
