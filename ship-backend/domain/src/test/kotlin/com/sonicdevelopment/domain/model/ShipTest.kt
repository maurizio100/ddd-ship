package com.sonicdevelopment.domain.model

import com.sonicdevelopment.domain.exception.ShipTooHeavyException
import com.sonicdevelopment.domain.fixtures.aCargo
import com.sonicdevelopment.domain.fixtures.aShip
import com.sonicdevelopment.domain.model.enums.ShippingState
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.ShippingQuote
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

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
}
