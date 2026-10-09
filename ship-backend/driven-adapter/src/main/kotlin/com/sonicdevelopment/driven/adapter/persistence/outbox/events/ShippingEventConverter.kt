package com.sonicdevelopment.driven.adapter.persistence.outbox.events

import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.driven.adapter.persistence.outbox.events.ShippingEvent.ShippingEventData.CargoEventData

object ShippingEventConverter {

    fun toShippingEvent(foundShip: Ship, originHarbor: HarborName): ShippingEvent {
        val shipping = foundShip.activeShipping ?: throw IllegalStateException()
        val destinationHarbor = shipping.destinationHarbor
            ?: throw IllegalStateException("A released Shipping must have a Destination Harbor")
        return ShippingEvent(
            shipEventData = ShippingEvent.ShipEventData(
                shipId = foundShip.id.id, shipName = foundShip.shipName, homeHarbor = foundShip.homeHarbor.name,
                earnings = "0.00"
            ),
            catain = ShippingEvent.CatainEventData(
                catainId = foundShip.catainId.id, catainName = foundShip.catainName
            ),
            shippingEventData = ShippingEvent.ShippingEventData(
                shippingId = shipping.id.id, shippingQuote = shipping.shippingQuote?.quote,
                cargo = foundShip.loadedCargo.map {
                    CargoEventData(it.id.id, it.name)
                },
                weight = foundShip.weight,
                originHarbor = originHarbor.name,
                destinationHarbor = destinationHarbor.name
            )
        )
    }

}