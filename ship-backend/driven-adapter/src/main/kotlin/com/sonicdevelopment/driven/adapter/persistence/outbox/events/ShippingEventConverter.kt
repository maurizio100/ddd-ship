package com.sonicdevelopment.driven.adapter.persistence.outbox.events

import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.driven.adapter.persistence.outbox.events.ShippingEvent.ShippingEventData.CargoEventData

object ShippingEventConverter {

    fun toShippingEvent(foundShip: Ship): ShippingEvent {
        val shipping = foundShip.activeShipping ?: throw IllegalStateException()
        return ShippingEvent(
            shipEventData = ShippingEvent.ShipEventData(
                shipId = foundShip.id.id, shipName = foundShip.shipName
            ),
            catain = ShippingEvent.CatainEventData(
                catainId = foundShip.catainId.id, catainName = "catain"
            ),
            shippingEventData = ShippingEvent.ShippingEventData(
                shippingId = shipping.id.id, shippingQuote = shipping.shippingQuote?.quote,
                cargo = foundShip.loadedCargo.map {
                    CargoEventData(it.id.id, it.name)
                },
                weight = foundShip.weight
            )
        )
    }

}