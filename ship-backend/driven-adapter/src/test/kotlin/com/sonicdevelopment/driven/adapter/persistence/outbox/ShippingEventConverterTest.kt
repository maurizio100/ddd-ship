package com.sonicdevelopment.driven.adapter.persistence.outbox

import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.Shipping
import com.sonicdevelopment.domain.model.enums.ShippingState
import com.sonicdevelopment.domain.model.values.CatainId
import com.sonicdevelopment.domain.model.values.HarborName
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

    private fun aShipWith(shipping: Shipping) = Ship(
        id = com.sonicdevelopment.domain.model.values.ShipId(UUID.randomUUID()),
        name = "Black Pearl",
        catainId = CatainId(UUID.randomUUID()),
        catainName = "Furry Jones",
        activeShipping = shipping,
        cargoLoad = mutableMapOf(),
    )
}
