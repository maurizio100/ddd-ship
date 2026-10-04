package com.sonicdevelopment.driven.adapter.persistence.outbox.events

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

/**
 * Payload of `ship-arrived`: the ship of the Shipping [shippingId] has arrived at [destinationHarbor].
 * Addressed to [originHarbor], the Harbor that Released it.
 */
data class ShipArrivedEvent @JsonCreator constructor(
    @JsonProperty("shipId") val shipId: UUID,
    @JsonProperty("shipName") val shipName: String,
    @JsonProperty("shippingId") val shippingId: UUID,
    @JsonProperty("originHarbor") val originHarbor: String,
    @JsonProperty("destinationHarbor") val destinationHarbor: String
)
