package com.sonicdevelopment.domain.model

import com.sonicdevelopment.domain.exception.NewShippingRefusedException
import com.sonicdevelopment.domain.exception.RefusedCargoException
import com.sonicdevelopment.domain.exception.ShipNotIncomingException
import com.sonicdevelopment.domain.exception.ShipTooHeavyException
import com.sonicdevelopment.domain.exception.ShippingNotPreparingException
import com.sonicdevelopment.domain.fixtures.aCargo
import com.sonicdevelopment.domain.fixtures.aShip
import com.sonicdevelopment.domain.model.enums.ShippingState
import com.sonicdevelopment.domain.model.values.CatainId
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.Money
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
    fun `loading the same Cargo again adds a second instance and sums its weight`() {
        val rum = aCargo(name = "Rum", weight = 5.5F)
        val ship = aShip(loadedCargo = listOf(rum))

        ship.addCargo(aCargo(id = rum.id, name = "Rum", weight = 5.5F))

        ship.loadedCargo.map { it.name } shouldBe listOf("Rum", "Rum")
        ship.weight shouldBe 11.0F
    }

    @Test
    fun `removing one of several loaded instances of the same Cargo keeps the other aboard`() {
        val rum = aCargo(name = "Rum", weight = 5.5F)
        val ship = aShip(loadedCargo = listOf(rum, aCargo(id = rum.id, name = "Rum", weight = 5.5F)))

        val removed = ship.removeCargo(rum)

        removed shouldBe true
        ship.loadedCargo.map { it.name } shouldBe listOf("Rum")
        ship.weight shouldBe 5.5F
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

    @Test
    fun `an Incoming Ship cannot get a new Shipping`() {
        val ship = anIdleShip(cargoAboard = listOf(aCargo(name = "Rum")), incoming = true)

        shouldThrow<NewShippingRefusedException> { ship.createNewShipping() }
            .message shouldBe "Salty Whisker must be unloaded or refused first"
        ship.activeShipping shouldBe null
        ship.isIncoming shouldBe true
    }

    @Test
    fun `an Incoming Ship must have Cargo aboard`() {
        shouldThrow<IllegalArgumentException> { anIdleShip(cargoAboard = emptyList(), incoming = true) }
    }

    @Test
    fun `a ship with Cargo aboard that is not Incoming can get a new Shipping`() {
        // a ship refused by its own Home Harbor keeps its Cargo aboard as an ordinary ship
        val rum = aCargo(name = "Rum")
        val ship = anIdleShip(cargoAboard = listOf(rum, rum), incoming = false)

        ship.createNewShipping()

        ship.shippingState() shouldBe ShippingState.PREPARING
        ship.isIncoming shouldBe false
        ship.cargoAboard.map { it.name } shouldBe listOf("Rum", "Rum")
    }

    @Test
    fun `a ship with an Active Shipping cannot get another`() {
        val ship = aShip(name = "Black Pearl")
        val preparing = ship.activeShipping!!

        shouldThrow<NewShippingRefusedException> { ship.createNewShipping() }
            .message shouldBe "Black Pearl already has an Active Shipping"
        ship.activeShipping shouldBe preparing
    }

    @Test
    fun `a ship with no Cargo aboard and no flag is not Incoming`() {
        val ship = anIdleShip()

        ship.isIncoming shouldBe false
        ship.cargoAboard shouldBe emptyList()
    }

    @Test
    fun `unloading an Incoming Ship returns its Cargo aboard and clears the Cargo aboard and the Incoming flag together`() {
        val rum = aCargo(name = "Rum")
        val sugar = aCargo(name = "Sugar", weight = 0.7F)
        val ship = anIdleShip(cargoAboard = listOf(rum, rum, sugar), incoming = true)

        val unloaded = ship.unload()

        unloaded shouldBe listOf(rum, rum, sugar)
        ship.cargoAboard shouldBe emptyList()
        ship.isIncoming shouldBe false
    }

    @Test
    fun `a ship that is not Incoming cannot be unloaded and keeps its Cargo aboard`() {
        // Cargo aboard without the flag: refused by its own Home Harbor
        val rum = aCargo(name = "Rum")
        val ship = anIdleShip(cargoAboard = listOf(rum), incoming = false)

        shouldThrow<ShipNotIncomingException> { ship.unload() }
            .message shouldBe "Salty Whisker is not an Incoming Ship"
        ship.cargoAboard shouldBe listOf(rum)
        ship.isIncoming shouldBe false
    }

    @Test
    fun `an Incoming Ship can be unloaded only once`() {
        val ship = anIdleShip(cargoAboard = listOf(aCargo(name = "Rum")), incoming = true)
        ship.unload()

        shouldThrow<ShipNotIncomingException> { ship.unload() }
    }

    @Test
    fun `refusing an Incoming Ship moves every Cargo aboard into the Loaded Cargo of a new Shipping`() {
        val rum = aCargo(name = "Rum", weight = 5.5F)
        val sugar = aCargo(name = "Sugar", weight = 0.7F)
        val ship = anIdleShip(cargoAboard = listOf(rum, rum, sugar), incoming = true)

        ship.refuse(HarborName("Tortuga"))

        ship.activeShipping shouldNotBe null
        ship.shippingState() shouldBe ShippingState.PREPARING
        ship.loadedCargo shouldBe listOf(rum, rum, sugar)
        ship.cargoAboard shouldBe emptyList()
        ship.isIncoming shouldBe false
    }

    @Test
    fun `a refused ship can be Released to its Home Harbor`() {
        val rum = aCargo(name = "Rum", weight = 5.5F)
        val ship = anIdleShip(cargoAboard = listOf(rum, rum), incoming = true)
        ship.refuse(HarborName("Tortuga"))

        ship.release(ShippingQuote("Fair winds"), ship.homeHarbor)

        ship.shippingState() shouldBe ShippingState.SHIPPING
        ship.activeShipping!!.destinationHarbor shouldBe HarborName("Port Royal")
        ship.weight shouldBe 11.0F
    }

    @Test
    fun `a ship that is not Incoming cannot be refused and changes nothing`() {
        val ship = anIdleShip()

        shouldThrow<ShipNotIncomingException> { ship.refuse(HarborName("Tortuga")) }
            .message shouldBe "Salty Whisker is not an Incoming Ship"
        ship.activeShipping shouldBe null
        ship.loadedCargo shouldBe emptyList()
    }

    @Test
    fun `an Incoming Ship refused at its Home Harbor is no longer Incoming, keeps its Cargo aboard and gets no Shipping`() {
        val rum = aCargo(name = "Rum")
        val ship = anIdleShip(cargoAboard = listOf(rum, rum), incoming = true)

        ship.refuse(HarborName("Port Royal"))

        ship.isIncoming shouldBe false
        ship.cargoAboard shouldBe listOf(rum, rum)
        ship.activeShipping shouldBe null
        ship.loadedCargo shouldBe emptyList()
    }

    @Test
    fun `a ship that is not Incoming cannot be refused at its Home Harbor either`() {
        val ship = anIdleShip(cargoAboard = listOf(aCargo(name = "Rum")), incoming = false)

        shouldThrow<ShipNotIncomingException> { ship.refuse(HarborName("Port Royal")) }
        ship.cargoAboard.size shouldBe 1
    }

    @Test
    fun `a ship refused away from home keeps the weight of its Cargo, counted once`() {
        val rum = aCargo(name = "Rum", weight = 5.5F)
        val ship = anIdleShip(cargoAboard = listOf(rum, rum), incoming = true)
        ship.weight shouldBe 11.0F

        ship.refuse(HarborName("Tortuga"))

        ship.weight shouldBe 11.0F
    }

    @Test
    fun `the Current Weight counts the Cargo aboard and a load beyond the Max Weight is refused`() {
        val rum = aCargo(name = "Rum", weight = 5.5F)
        val ship = homeRefusedShip(rum, rum)
        ship.weight shouldBe 11.0F

        shouldThrow<ShipTooHeavyException> { ship.addCargo(aCargo(name = "Anvil", weight = 4.5F)) }
        ship.loadedCargo shouldBe emptyList()

        ship.addCargo(aCargo(name = "Silk", weight = 4.0F))
        ship.weight shouldBe 15.0F
    }

    @Test
    fun `a Cargo only aboard cannot be removed and nothing changes`() {
        val rum = aCargo(name = "Rum", weight = 5.5F)
        val ship = homeRefusedShip(rum, rum)

        shouldThrow<RefusedCargoException> { ship.removeCargo(rum) }
            .message shouldBe "Rum aboard Salty Whisker was refused here and can only be delivered to another Harbor"

        ship.cargoAboard shouldBe listOf(rum, rum)
        ship.weight shouldBe 11.0F
    }

    @Test
    fun `a loaded Cargo that is also aboard is removed from the Loaded Cargo`() {
        val rum = aCargo(name = "Rum", weight = 5.5F)
        val ship = homeRefusedShip(rum)
        ship.addCargo(rum)

        ship.removeCargo(rum) shouldBe true

        ship.loadedCargo shouldBe emptyList()
        ship.cargoAboard shouldBe listOf(rum)
        ship.weight shouldBe 5.5F
    }

    @Test
    fun `a Cargo neither loaded nor aboard is still silently not removed`() {
        val ship = homeRefusedShip(aCargo(name = "Rum"))

        ship.removeCargo(aCargo(name = "Silk")) shouldBe false
    }

    @Test
    fun `releasing a ship with Cargo aboard moves it into the Loaded Cargo`() {
        val rum = aCargo(name = "Rum", weight = 5.5F)
        val silk = aCargo(name = "Silk", weight = 1.0F)
        val ship = homeRefusedShip(rum, rum)
        ship.addCargo(silk)

        ship.release(ShippingQuote("Fair winds"), HarborName("Tortuga"))

        ship.loadedCargo shouldBe listOf(silk, rum, rum)
        ship.cargoAboard shouldBe emptyList()
        ship.weight shouldBe 12.0F
        ship.shippingState() shouldBe ShippingState.SHIPPING
    }

    private fun homeRefusedShip(vararg cargo: Cargo) =
        anIdleShip(cargoAboard = cargo.toList(), incoming = true).apply {
            refuse(HarborName("Port Royal"))
            createNewShipping()
        }

    private fun anIdleShip(cargoAboard: List<Cargo> = emptyList(), incoming: Boolean = false) = Ship(
        name = "Salty Whisker",
        catainId = CatainId(UUID.randomUUID()),
        catainName = "Furry Jones",
        homeHarbor = HarborName("Port Royal"),
        cargoAboard = cargoAboard,
        incoming = incoming,
    )

    @Test
    fun `earn adds the Delivery Price to the Earnings away from the Home Harbor`() {
        val ship = Ship(
            name = "Salty Whisker", catainId = CatainId(UUID.randomUUID()), catainName = "Furry Jones",
            homeHarbor = HarborName("Port Royal"), earnings = Money.of("80.00"),
        )

        val earned = ship.earn(Money.of("40.00"), HarborName("Isla de Muerta"))

        earned shouldBe true
        ship.earnings.toDecimalString() shouldBe "120.00"
    }

    @Test
    fun `earn changes nothing at the Home Harbor and returns false`() {
        val ship = aShip(homeHarbor = HarborName("Port Royal"))

        val earned = ship.earn(Money.of("80.00"), HarborName("Port Royal"))

        earned shouldBe false
        ship.earnings.toDecimalString() shouldBe "0.00"
    }
}
