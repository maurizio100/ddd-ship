package com.sonicdevelopment.driven.adapter.persistence.outbox.events

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable
import java.util.*


data class ShippingEvent @JsonCreator constructor(
    @JsonProperty("shipEventData") val shipEventData: ShipEventData,
    @JsonProperty("shippingEventData") val shippingEventData: ShippingEventData,
    @JsonProperty("catain") val catain: CatainEventData
) : Serializable {

    data class ShipEventData @JsonCreator constructor(
        @JsonProperty("shipId") val shipId: UUID,
        @JsonProperty("shipName") val shipName: String
    ) : Serializable

    data class CatainEventData @JsonCreator constructor(
        @JsonProperty("catainId") val catainId: UUID,
        @JsonProperty("catainName") val catainName: String
    ) : Serializable

    data class ShippingEventData @JsonCreator constructor(
        @JsonProperty("shippingId") val shippingId: UUID,
        @JsonProperty("weight") val weight: Float,
        @JsonProperty("shippingQuote") val shippingQuote: String?,
        @JsonProperty("cargo") val cargo: List<CargoEventData>
    ) : Serializable {
        data class CargoEventData @JsonCreator constructor(
            @JsonProperty("cargoId") val cargoId: UUID,
            @JsonProperty("cargoName") val cargoName: String
        ) : Serializable
    }
}
