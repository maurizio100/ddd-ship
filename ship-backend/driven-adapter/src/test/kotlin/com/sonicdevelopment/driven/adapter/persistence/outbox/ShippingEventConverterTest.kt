package com.sonicdevelopment.driven.adapter.persistence.outbox

import com.fasterxml.jackson.databind.ObjectMapper
import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.Shipping
import com.sonicdevelopment.domain.model.enums.ShipClass
import com.sonicdevelopment.domain.model.enums.ShippingState
import com.sonicdevelopment.domain.model.values.CatainId
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.domain.model.values.ShippingId
import com.sonicdevelopment.domain.model.values.ShippingQuote
import com.sonicdevelopment.driven.adapter.persistence.outbox.events.ShippingEvent
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

    @Test
    fun `shipping-published carries whether the ship sails home imprisoned`() {
        val shipping = Shipping(
            ShippingId(UUID.randomUUID()), ShippingQuote("Fair winds"), ShippingState.SHIPPING, HarborName("Isla de Muerta")
        )

        val imprisoned = ShippingEventConverter.toShippingEvent(aShipWith(shipping, imprisoned = true), HarborName("Tortuga"))
        val free = ShippingEventConverter.toShippingEvent(aShipWith(shipping), HarborName("Tortuga"))

        imprisoned.shipEventData.imprisoned shouldBe true
        ObjectMapper().readTree(ObjectMapper().writeValueAsString(imprisoned))
            .path("shipEventData").path("imprisoned").asBoolean() shouldBe true
        free.shipEventData.imprisoned shouldBe false
    }

    @Test
    fun `shipping-published carries the ship's Ship Class`() {
        val shipping = Shipping(
            ShippingId(UUID.randomUUID()), ShippingQuote("Fair winds"), ShippingState.SHIPPING, HarborName("Isla de Muerta")
        )

        val event = ShippingEventConverter.toShippingEvent(aShipWith(shipping, shipClass = ShipClass.BRIG), HarborName("Tortuga"))

        event.shipEventData.shipClass shouldBe "BRIG"
        ObjectMapper().readTree(ObjectMapper().writeValueAsString(event))
            .path("shipEventData").path("shipClass").asText() shouldBe "BRIG"
    }

    @Test
    fun `shipping-published carries the ship's Crew`() {
        val shipping = Shipping(
            ShippingId(UUID.randomUUID()), ShippingQuote("Fair winds"), ShippingState.SHIPPING, HarborName("Isla de Muerta")
        )
        val mittens = com.sonicdevelopment.domain.model.CrewMember(
            com.sonicdevelopment.domain.model.values.CrewMemberId(UUID.randomUUID()), "Mittens", "Strong Paws"
        )
        val patch = com.sonicdevelopment.domain.model.CrewMember(
            com.sonicdevelopment.domain.model.values.CrewMemberId(UUID.randomUUID()), "Patch", "Sharp Eyes"
        )

        val event = ShippingEventConverter.toShippingEvent(aShipWith(shipping, crew = listOf(mittens, patch)), HarborName("Tortuga"))
        val without = ShippingEventConverter.toShippingEvent(aShipWith(shipping), HarborName("Tortuga"))

        event.shipEventData.crew shouldBe listOf(
            ShippingEvent.CrewMemberEventData(mittens.id.id, "Mittens", "Strong Paws"),
            ShippingEvent.CrewMemberEventData(patch.id.id, "Patch", "Sharp Eyes"),
        )
        val json = ObjectMapper().readTree(ObjectMapper().writeValueAsString(event)).path("shipEventData").path("crew")
        json[1].path("crewMemberId").asText() shouldBe patch.id.id.toString()
        json[1].path("trait").asText() shouldBe "Sharp Eyes"
        without.shipEventData.crew shouldBe emptyList()
    }

    private fun aShipWith(
        shipping: Shipping, earnings: Money = Money.dollars(0), imprisoned: Boolean = false, shipClass: ShipClass = ShipClass.SLOOP,
        crew: List<com.sonicdevelopment.domain.model.CrewMember> = emptyList(),
    ) = Ship(
        id = com.sonicdevelopment.domain.model.values.ShipId(UUID.randomUUID()),
        name = "Black Pearl",
        catainId = CatainId(UUID.randomUUID()),
        catainName = "Furry Jones",
        homeHarbor = HarborName("Isla de Muerta"),
        activeShipping = shipping,
        cargoLoad = mutableListOf(),
        earnings = earnings,
        imprisoned = imprisoned,
        shipClass = shipClass,
        crew = crew,
    )
}
