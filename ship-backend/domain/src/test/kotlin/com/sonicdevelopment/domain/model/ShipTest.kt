package com.sonicdevelopment.domain.model

import com.sonicdevelopment.domain.exception.ShipTooHeavyException
import com.sonicdevelopment.domain.exception.ShippingNotPreparingException
import com.sonicdevelopment.domain.fixtures.aCargo
import com.sonicdevelopment.domain.fixtures.aShip
import com.sonicdevelopment.domain.model.enums.ShippingState
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.ShippingId
import com.sonicdevelopment.domain.model.values.ShippingQuote
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import java.util.*

class ShipTest {

    @Test
    fun `removing Cargo that is not loaded returns false and keeps the Current Weight`() {
        val ship = aShip(loadedCargo = listOf(aCargo(name = "Ale", weight = 2.0F)))

        val removed = ship.removeCargo(aCargo(name = "Rum", weight = 5.5F))

        removed shouldBe false
        ship.weight shouldBe 2.0F
        ship.loadedCargo.map { it.name } shouldBe listOf("Ale")
    }

    @Test
    fun `removing loaded Cargo returns true and lowers the Current Weight`() {
        val rum = aCargo(name = "Rum", weight = 5.5F)
        val ship = aShip(loadedCargo = listOf(aCargo(name = "Ale", weight = 2.0F), rum))

        val removed = ship.removeCargo(rum)

        removed shouldBe true
        ship.weight shouldBe 2.0F
        ship.loadedCargo.map { it.name } shouldBe listOf("Ale")
    }

    @Test
    fun `a too-heavy load names the Cargo and the Max Weight`() {
        val ship = aShip(loadedCargo = listOf(aCargo(name = "Planks", weight = 14.0F)))

        val rejection = shouldThrow<ShipTooHeavyException> { ship.addCargo(aCargo(name = "Rum", weight = 5.5F)) }

        rejection.message shouldBe "Loading Rum would exceed the Max Weight of 15.0"
    }

    @Test
    fun `release sets the quote, state SHIPPING and the Destination Harbor`() {
        val ship = aShip()

        ship.release(ShippingQuote("Fair winds"), HarborName("Port Royal"))

        ship.shippingState() shouldBe ShippingState.SHIPPING
        ship.activeShipping!!.shippingQuote shouldBe ShippingQuote("Fair winds")
        ship.activeShipping!!.destinationHarbor shouldBe HarborName("Port Royal")
    }

    @Test
    fun `a ship that is already at sea cannot be Released again and keeps its Destination Harbor`() {
        val ship = aShip(name = "Black Pearl")
        ship.release(ShippingQuote("Fair winds"), HarborName("Port Royal"))

        val rejection = shouldThrow<ShippingNotPreparingException> {
            ship.release(ShippingQuote("Rough seas"), HarborName("Nassau"))
        }

        rejection.message shouldBe "Black Pearl is not being prepared"
        ship.shippingState() shouldBe ShippingState.SHIPPING
        ship.activeShipping!!.shippingQuote shouldBe ShippingQuote("Fair winds")
        ship.activeShipping!!.destinationHarbor shouldBe HarborName("Port Royal")
    }

    @Test
    fun `a ship without a Shipping cannot be Released`() {
        val ship = aShip(name = "Black Pearl", activeShipping = null)

        shouldThrow<ShippingNotPreparingException> {
            ship.release(ShippingQuote("Fair winds"), HarborName("Port Royal"))
        }.message shouldBe "Black Pearl is not being prepared"
    }

    @Test
    fun `ending a voyage sets its Shipping to DONE`() {
        val ship = aShip()
        ship.release(ShippingQuote("Fair winds"), HarborName("Port Royal"))
        val voyage = ship.activeShipping!!

        val ended = ship.endShipping(voyage.id)

        ended shouldBe true
        voyage.shippingState shouldBe ShippingState.DONE
        ship.shippingState() shouldBe ShippingState.DONE
    }

    @Test
    fun `only the Active Shipping at sea with that id can end`() {
        val atSea = aShip()
        atSea.release(ShippingQuote("Fair winds"), HarborName("Port Royal"))
        val beingPrepared = aShip()
        val withoutShipping = aShip(activeShipping = null)

        atSea.endShipping(ShippingId(UUID.randomUUID())) shouldBe false
        beingPrepared.endShipping(beingPrepared.activeShipping!!.id) shouldBe false
        withoutShipping.endShipping(ShippingId(UUID.randomUUID())) shouldBe false

        atSea.shippingState() shouldBe ShippingState.SHIPPING
        beingPrepared.shippingState() shouldBe ShippingState.PREPARING
        withoutShipping.shippingState() shouldBe ShippingState.IDLE
        withoutShipping.activeShipping shouldBe null
    }

    @Test
    fun `a ship whose Shipping is DONE can get a new Shipping`() {
        val ship = aShip()
        ship.release(ShippingQuote("Fair winds"), HarborName("Port Royal"))
        val voyage = ship.activeShipping!!
        ship.endShipping(voyage.id)

        ship.createNewShipping()

        ship.shippingState() shouldBe ShippingState.PREPARING
        ship.activeShipping!!.id shouldNotBe voyage.id
    }
}
