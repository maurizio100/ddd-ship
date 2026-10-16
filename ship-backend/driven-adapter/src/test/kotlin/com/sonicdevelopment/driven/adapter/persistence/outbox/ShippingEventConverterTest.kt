package com.sonicdevelopment.driven.adapter.persistence.outbox

import com.fasterxml.jackson.databind.ObjectMapper
import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.Shipping
import com.sonicdevelopment.domain.model.enums.ShippingState
import com.sonicdevelopment.domain.model.values.CatainId
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.domain.model.values.ShippingId
import com.sonicdevelopment.domain.model.values.ShippingQuote
import com.sonicdevelopment.driven.adapter.persistence.outbox.events.ShippingEventConverter
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.util.*

class ShippingEventConverterTest {

    @Test
    fun `a released Shipping without a Destination Harbor cannot be converted`() {
        val ship = aShipWith(
            Shipping(
                ShippingId(UUID.randomUUID()), ShippingQuote("Fair winds"), ShippingState.SHIPPING, destinationHarbor = null
            )
        )

        val failure = shouldThrow<IllegalStateException> {
            ShippingEventConverter.toShippingEvent(ship, HarborName("Tortuga"))
        }

        failure.message shouldBe "A released Shipping must have a Destination Harbor"
    }

    @Test
    fun `a released Shipping names the Origin Harbor and the Destination Harbor`() {
        val ship = aShipWith(
            Shipping(
                ShippingId(UUID.randomUUID()), ShippingQuote("Fair winds"), ShippingState.SHIPPING, HarborName("Port Royal")
            )
        )

        val event = ShippingEventConverter.toShippingEvent(ship, HarborName("Tortuga"))

        event.shippingEventData.originHarbor shouldBe "Tortuga"
        event.shippingEventData.destinationHarbor shouldBe "Port Royal"
    }

    @Test
    fun `a released Shipping carries the Home Harbor of its ship under shipEventData`() {
        val ship = aShipWith(
            Shipping(
                ShippingId(UUID.randomUUID()), ShippingQuote("Fair winds"), ShippingState.SHIPPING, HarborName("Port Royal")
            )
        )

        val event = ShippingEventConverter.toShippingEvent(ship, HarborName("Tortuga"))

        event.shipEventData.homeHarbor shouldBe "Isla de Muerta"
        ObjectMapper().readTree(ObjectMapper().writeValueAsString(event))
            .path("shipEventData").path("homeHarbor").asText() shouldBe "Isla de Muerta"
    }

    @Test
    fun `shipping-published carries the ship's Earnings as a two-decimal string`() {
        val ship = aShipWith(
            Shipping(
                ShippingId(UUID.randomUUID()), ShippingQuote("Fair winds"), ShippingState.SHIPPING, HarborName("Port Royal")
            ),
            earnings = Money.of("120.5"),
        )

        val event = ShippingEventConverter.toShippingEvent(ship, HarborName("Tortuga"))

        event.shipEventData.earnings shouldBe "120.50"
        ObjectMapper().readTree(ObjectMapper().writeValueAsString(event))
            .path("shipEventData").path("earnings").asText() shouldBe "120.50"
    }

    private fun aShipWith(shipping: Shipping, earnings: Money = Money.dollars(0)) = Ship(
        id = com.sonicdevelopment.domain.model.values.ShipId(UUID.randomUUID()),
        name = "Black Pearl",
        catainId = CatainId(UUID.randomUUID()),
        catainName = "Furry Jones",
        homeHarbor = HarborName("Isla de Muerta"),
        activeShipping = shipping,
        cargoLoad = mutableListOf(),
        earnings = earnings,
    )
}
