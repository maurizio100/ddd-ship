package com.sonicdevelopment.application.acceptance.fixtures

import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.values.CargoId
import com.sonicdevelopment.domain.model.values.CatainId
import com.sonicdevelopment.domain.ports.driven.CargoPersistencePort
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort
import com.sonicdevelopment.domain.model.values.EventId
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.model.values.ShippingId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.transaction.support.TransactionTemplate
import java.util.UUID

class FakeDrivenPortsTest {

    private val rum = CargoId(SeedData.cargoIdOf("Rum"))

    @Test
    fun `afterCommit fires for a synchronization registered in a nested transaction`() {
        val template = TransactionTemplate(InMemoryTransactionManager())
        var committed = 0
        template.executeWithoutResult {
            template.executeWithoutResult {
                TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
                    override fun afterCommit() {
                        committed++
                    }
                })
            }
            committed shouldBe 0
        }
        committed shouldBe 1
    }

    @Test
    fun `afterCommit does not fire on rollback`() {
        val template = TransactionTemplate(InMemoryTransactionManager())
        var committed = 0
        template.executeWithoutResult { status ->
            TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
                override fun afterCommit() {
                    committed++
                }
            })
            status.setRollbackOnly()
        }
        committed shouldBe 0
    }

    @Test
    fun `putIntoStock adds to an existing entry and starts a missing one`() {
        val stock = InMemoryStock(InMemoryCargoCatalog())
        stock.putIntoStock(rum, 2)
        stock.getStock()[rum] shouldBe STARTING_STOCK + 2

        val unknown = CargoId(UUID.randomUUID())
        stock.putIntoStock(unknown)
        stock.getStock()[unknown] shouldBe 1
    }

    @Test
    fun `takeOneFromStock returns false at 0 and leaves it at 0`() {
        val stock = InMemoryStock(InMemoryCargoCatalog())
        stock.setQuantity(rum.id, 1)

        stock.takeOneFromStock(rum) shouldBe true
        stock.takeOneFromStock(rum) shouldBe false
        stock.getStock()[rum] shouldBe 0
    }

    @Test
    fun `the inbox and the arrivals report true only on the first write`() {
        val inbox = InMemoryInbox()
        val eventId = EventId(UUID.randomUUID())
        inbox.recordConsumedEvent(eventId) shouldBe true
        inbox.recordConsumedEvent(eventId) shouldBe false

        val arrivals = InMemoryArrivals()
        val shippingId = ShippingId(UUID.randomUUID())
        arrivals.recordArrival(shippingId, ShipId(UUID.randomUUID())) shouldBe true
        arrivals.recordArrival(shippingId, ShipId(UUID.randomUUID())) shouldBe false
    }

    @Test
    fun `the known Harbors keep one entry per name, sorted`() {
        val harbors = InMemoryKnownHarbors()
        listOf("Port Royal", "Nassau", "Port Royal").forEach { harbors.rememberHarbor(HarborName(it)) }

        harbors.getKnownHarbors() shouldBe listOf(HarborName("Nassau"), HarborName("Port Royal"))
    }

    @Test
    fun `the fleet keeps the same Cargo more than once and unloading removes one instance`() {
        val fleet = InMemoryFleet(InMemoryCatains())
        val rumCargo = SeedData.allCargo().single { it.id == rum }
        val ship = Ship(
            name = "Black Pearl", catainId = CatainId(SeedData.aCatainId), catainName = "Catain", homeHarbor = HarborName("Port Royal"),
        )
        ship.createNewShipping()
        val shipId = ship.id
        fleet.saveNewShip(ShipRepositoryPort.InitialShipInformation.fromShip(ship))
        fleet.createShipping(ship)

        ship.addCargo(rumCargo)
        ship.addCargo(rumCargo)
        fleet.updateCargoLoad(CargoPersistencePort.CargoLoadInformation.fromShip(ship))

        fleet.getShipDetails(shipId)!!.loadedCargo.map { it.id } shouldBe listOf(rum, rum)

        val unloading = fleet.getShipDetails(shipId)!!
        unloading.removeCargo(rumCargo) shouldBe true
        fleet.updateCargoLoad(CargoPersistencePort.CargoLoadInformation.fromShip(unloading))

        fleet.getShipDetails(shipId)!!.loadedCargo.map { it.id } shouldBe listOf(rum)
    }

    @Test
    fun `the fleet keeps the Cargo aboard a ship and its Incoming flag, the same Cargo more than once, and a rename keeps both`() {
        val fleet = InMemoryFleet(InMemoryCatains())
        val rumCargo = SeedData.allCargo().single { it.id == rum }
        val sugarCargo = SeedData.allCargo().single { it.name == "Sugar" }
        val ship = Ship(
            name = "Salty Whisker", catainId = CatainId(SeedData.aCatainId), catainName = "Catain", homeHarbor = HarborName("Port Royal"),
            cargoAboard = listOf(rumCargo, rumCargo, sugarCargo), incoming = true,
        )
        fleet.saveNewShip(ShipRepositoryPort.InitialShipInformation.fromShip(ship))

        val stored = fleet.getShipDetails(ship.id)!!
        stored.isIncoming shouldBe true
        stored.cargoAboard.map { it.id } shouldBe listOf(rum, rum, sugarCargo.id)
        fleet.getAllShips().single().isIncoming shouldBe true

        // a rename saves the loaded ship, which carries its Cargo aboard and its flag
        stored.shipName = "Salty Whiskers"
        fleet.saveNewShip(ShipRepositoryPort.InitialShipInformation.fromShip(stored))
        val renamed = fleet.getShipDetails(ship.id)!!
        renamed.shipName shouldBe "Salty Whiskers"
        renamed.isIncoming shouldBe true
        renamed.cargoAboard.map { it.id } shouldBe listOf(rum, rum, sugarCargo.id)

        // a save replaces the Cargo aboard, as the adapter does
        fleet.saveNewShip(
            ShipRepositoryPort.InitialShipInformation.fromShip(
                Ship(id = ship.id, name = "Salty Whisker", catainId = ship.catainId, catainName = "Catain", homeHarbor = HarborName("Port Royal"))
            )
        )
        fleet.getShipDetails(ship.id)!!.cargoAboard shouldBe emptyList()
        fleet.getShipDetails(ship.id)!!.isIncoming shouldBe false
    }

    @Test
    fun `unloading an Incoming Ship clears its flag and its Cargo aboard once, and reports false after`() {
        val fleet = InMemoryFleet(InMemoryCatains())
        val rumCargo = SeedData.allCargo().single { it.id == rum }
        val incoming = Ship(
            name = "Salty Whisker", catainId = CatainId(SeedData.aCatainId), catainName = "Catain", homeHarbor = HarborName("Port Royal"),
            cargoAboard = listOf(rumCargo, rumCargo), incoming = true,
        )
        val refused = Ship(
            name = "Refused Rover", catainId = CatainId(SeedData.aCatainId), catainName = "Catain", homeHarbor = HarborName("Port Royal"),
            cargoAboard = listOf(rumCargo), incoming = false,
        )
        fleet.saveNewShip(ShipRepositoryPort.InitialShipInformation.fromShip(incoming))
        fleet.saveNewShip(ShipRepositoryPort.InitialShipInformation.fromShip(refused))

        fleet.unloadIncomingShip(incoming.id) shouldBe true

        val unloaded = fleet.getShipDetails(incoming.id)!!
        unloaded.isIncoming shouldBe false
        unloaded.cargoAboard shouldBe emptyList()
        fleet.unloadIncomingShip(incoming.id) shouldBe false
        // a ship that is not Incoming, an unknown one and one that left the fleet are left alone
        fleet.unloadIncomingShip(refused.id) shouldBe false
        fleet.getShipDetails(refused.id)!!.cargoAboard.map { it.id } shouldBe listOf(rum)
        fleet.unloadIncomingShip(ShipId(UUID.randomUUID())) shouldBe false
        fleet.saveNewShip(ShipRepositoryPort.InitialShipInformation.fromShip(incoming))
        fleet.removeFromFleet(incoming.id)
        fleet.unloadIncomingShip(incoming.id) shouldBe false
    }

    @Test
    fun `endIncoming clears only the Incoming flag, and clearCargoAboard empties the Cargo aboard`() {
        val fleet = InMemoryFleet(InMemoryCatains())
        val rumCargo = SeedData.allCargo().single { it.id == rum }
        val incoming = Ship(
            name = "Salty Whisker", catainId = CatainId(SeedData.aCatainId), catainName = "Catain", homeHarbor = HarborName("Port Royal"),
            cargoAboard = listOf(rumCargo, rumCargo), incoming = true,
        )
        fleet.saveNewShip(ShipRepositoryPort.InitialShipInformation.fromShip(incoming))

        fleet.endIncoming(incoming.id) shouldBe true

        val ended = fleet.getShipDetails(incoming.id)!!
        ended.isIncoming shouldBe false
        ended.cargoAboard.map { it.id } shouldBe listOf(rum, rum)
        fleet.endIncoming(incoming.id) shouldBe false
        fleet.endIncoming(ShipId(UUID.randomUUID())) shouldBe false

        fleet.clearCargoAboard(incoming.id)

        fleet.getShipDetails(incoming.id)!!.cargoAboard shouldBe emptyList()
    }

    @Test
    fun `addEarnings mirrors the adapter - only a ship in the fleet`() {
        val fleet = InMemoryFleet(InMemoryCatains())
        val saltyWhisker = Ship(
            name = "Salty Whisker", catainId = CatainId(SeedData.aCatainId), catainName = "Catain", homeHarbor = HarborName("Port Royal"),
            earnings = Money.of("80.00"),
        )
        fleet.saveNewShip(ShipRepositoryPort.InitialShipInformation.fromShip(saltyWhisker))
        fleet.getShipDetails(saltyWhisker.id)!!.earnings.toDecimalString() shouldBe "80.00"

        fleet.addEarnings(saltyWhisker.id, Money.of("40.05"))

        fleet.getShipDetails(saltyWhisker.id)!!.earnings.toDecimalString() shouldBe "120.05"
        shouldThrow<IllegalStateException> { fleet.addEarnings(ShipId(UUID.randomUUID()), Money.of("1.00")) }
        fleet.removeFromFleet(saltyWhisker.id)
        shouldThrow<IllegalStateException> { fleet.addEarnings(saltyWhisker.id, Money.of("1.00")) }
    }

    @Test
    fun `rememberPrice keeps the first Price and reports false after`() {
        val prices = InMemoryPrices(InMemoryCargoCatalog())

        prices.rememberPrice(rum, Money.of("42.00")) shouldBe true
        prices.rememberPrice(rum, Money.of("57.00")) shouldBe false

        prices.getPrices() shouldBe mapOf(rum to Money.of("42.00"))
        prices.reset()
        prices.getPrices() shouldBe emptyMap()
    }

    @Test
    fun `rememberPrice ignores a Cargo outside the catalog`() {
        val prices = InMemoryPrices(InMemoryCargoCatalog())

        prices.rememberPrice(CargoId(UUID.randomUUID()), Money.of("42.00")) shouldBe false

        prices.getPrices() shouldBe emptyMap()
    }

    @Test
    fun `the Savings start at the Starting Savings`() {
        val savings = InMemorySavings()
        savings.getSavings() shouldBe Money.of(STARTING_SAVINGS)

        savings.setSavings("640.50")
        savings.getSavings().toDecimalString() shouldBe "640.50"

        savings.reset()
        savings.getSavings().toDecimalString() shouldBe STARTING_SAVINGS
    }

    @Test
    fun `paying from the Savings mirrors the adapter's conditional update`() {
        val savings = InMemorySavings()
        savings.setSavings("150.00")

        savings.pay(Money.of("100.00")) shouldBe true
        savings.getSavings().toDecimalString() shouldBe "50.00"

        savings.pay(Money.of("50.00")) shouldBe true
        savings.getSavings().toDecimalString() shouldBe "0.00"

        savings.setSavings("80.00")
        savings.pay(Money.of("100.00")) shouldBe false
        savings.getSavings().toDecimalString() shouldBe "80.00"
    }

    @Test
    fun `receive adds to the Savings`() {
        val savings = InMemorySavings()
        savings.setSavings("920.00")

        savings.receive(Money.of("80.05"))

        savings.getSavings().toDecimalString() shouldBe "1000.05"
    }
}
