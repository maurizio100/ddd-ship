package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.exception.CargoHasNoPriceException
import com.sonicdevelopment.domain.exception.SavingsDoNotCoverException
import com.sonicdevelopment.domain.exception.ShipNotIncomingException
import com.sonicdevelopment.domain.fixtures.aCargo
import com.sonicdevelopment.domain.model.Cargo
import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.values.CatainId
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.model.enums.ShippingState
import com.sonicdevelopment.domain.model.values.ShippingQuote
import com.sonicdevelopment.domain.ports.driven.CargoPersistencePort
import com.sonicdevelopment.domain.ports.driven.PriceRepositoryPort
import com.sonicdevelopment.domain.ports.driven.QuoteRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShippingOutboxRepository
import com.sonicdevelopment.domain.ports.driven.ShippingRepositoryPort
import com.sonicdevelopment.domain.ports.driven.SavingsRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort
import com.sonicdevelopment.domain.ports.driven.StockRepositoryPort
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyOrder
import jakarta.transaction.Transactional
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class IncomingShipManagementServiceTest {

    private val shipRepositoryPort = mockk<ShipRepositoryPort>()
    private val priceRepositoryPort = mockk<PriceRepositoryPort>()
    private val savingsRepositoryPort = mockk<SavingsRepositoryPort>()
    private val stockRepositoryPort = mockk<StockRepositoryPort>(relaxed = true)

    private val shippingRepositoryPort = mockk<ShippingRepositoryPort>(relaxed = true)
    private val cargoPersistencePort = mockk<CargoPersistencePort>()
    private val quoteRepositoryPort = mockk<QuoteRepositoryPort>()
    private val shippingOutboxRepository = mockk<ShippingOutboxRepository>(relaxed = true)
    private val tortuga = HarborName("Tortuga")

    private val service = IncomingShipManagementService(
        shipRepositoryPort, priceRepositoryPort, savingsRepositoryPort, stockRepositoryPort,
        shippingRepositoryPort, cargoPersistencePort, quoteRepositoryPort, shippingOutboxRepository, tortuga,
    )

    private val rum = aCargo(name = "Rum")
    private val sugar = aCargo(name = "Sugar", weight = 0.7F)
    private val saltyWhiskerId = ShipId(UUID.randomUUID())

    @BeforeEach
    fun saltyWhiskerArrivedWithTwoRumAndOneSugar() {
        every { shipRepositoryPort.getShipDetails(saltyWhiskerId) } returns
            aShip(cargoAboard = listOf(rum, rum, sugar), incoming = true)
        every { priceRepositoryPort.getPrices() } returns mapOf(rum.id to Money.of("40.00"), sugar.id to Money.of("35.00"))
        every { savingsRepositoryPort.pay(any()) } returns true
        every { shipRepositoryPort.unloadIncomingShip(saltyWhiskerId) } returns true
        every { cargoPersistencePort.updateCargoLoad(any()) } answers { firstArg() }
        every { quoteRepositoryPort.getQuoteForSailorsCode(any()) } returns ShippingQuote("Fair winds")
    }

    @Test
    fun `unloading pays the Delivery Price, then puts each Cargo into the Stock, then clears the ship`() {
        val paid = service.unloadIncomingShip(saltyWhiskerId)

        paid shouldBe Money.of("115.00")
        verifyOrder {
            savingsRepositoryPort.pay(Money.of("115.00"))
            stockRepositoryPort.putIntoStock(rum.id, 1)
            stockRepositoryPort.putIntoStock(rum.id, 1)
            stockRepositoryPort.putIntoStock(sugar.id, 1)
            shipRepositoryPort.unloadIncomingShip(saltyWhiskerId)
        }
        verify(exactly = 1) { savingsRepositoryPort.pay(any()) }
        verify(exactly = 3) { stockRepositoryPort.putIntoStock(any(), any()) }
    }

    @Test
    fun `an unloading the Savings cannot cover is refused with the Delivery Price and changes nothing else`() {
        every { savingsRepositoryPort.pay(Money.of("115.00")) } returns false

        val refusal = shouldThrow<SavingsDoNotCoverException> { service.unloadIncomingShip(saltyWhiskerId) }

        refusal.cost shouldBe Money.of("115.00")
        refusal.message shouldBe "The Savings do not cover the Delivery Price of 115.00 $"
        verify(exactly = 0) { stockRepositoryPort.putIntoStock(any(), any()) }
        verify(exactly = 0) { shipRepositoryPort.unloadIncomingShip(any()) }
    }

    @Test
    fun `a Cargo aboard without a Price refuses the unloading before anything is paid`() {
        every { priceRepositoryPort.getPrices() } returns mapOf(rum.id to Money.of("40.00"))

        val refusal = shouldThrow<CargoHasNoPriceException> { service.unloadIncomingShip(saltyWhiskerId) }

        refusal.message shouldBe "Sugar has no Price yet, so Salty Whisker cannot be unloaded"
        verify(exactly = 0) { savingsRepositoryPort.pay(any()) }
        verify(exactly = 0) { stockRepositoryPort.putIntoStock(any(), any()) }
        verify(exactly = 0) { shipRepositoryPort.unloadIncomingShip(any()) }
    }

    @Test
    fun `a ship that is not Incoming is refused before anything is paid`() {
        every { shipRepositoryPort.getShipDetails(saltyWhiskerId) } returns aShip(cargoAboard = emptyList(), incoming = false)

        shouldThrow<ShipNotIncomingException> { service.unloadIncomingShip(saltyWhiskerId) }
            .message shouldBe "Salty Whisker is not an Incoming Ship"

        verify(exactly = 0) { savingsRepositoryPort.pay(any()) }
        verify(exactly = 0) { stockRepositoryPort.putIntoStock(any(), any()) }
        verify(exactly = 0) { shipRepositoryPort.unloadIncomingShip(any()) }
    }

    @Test
    fun `a ship not in the fleet returns null and changes nothing`() {
        every { shipRepositoryPort.getShipDetails(saltyWhiskerId) } returns null

        service.unloadIncomingShip(saltyWhiskerId) shouldBe null

        verify(exactly = 0) { savingsRepositoryPort.pay(any()) }
        verify(exactly = 0) { stockRepositoryPort.putIntoStock(any(), any()) }
        verify(exactly = 0) { shipRepositoryPort.unloadIncomingShip(any()) }
    }

    @Test
    fun `an unloading that loses to a concurrent one fails after paying, so its transaction rolls the payment back`() {
        // a concurrent unloading of the same ship cleared it first: the conditional clear finds nothing to clear
        every { shipRepositoryPort.unloadIncomingShip(saltyWhiskerId) } returns false

        shouldThrow<ShipNotIncomingException> { service.unloadIncomingShip(saltyWhiskerId) }
            .message shouldBe "Salty Whisker is not an Incoming Ship anymore"

        verify(exactly = 1) { savingsRepositoryPort.pay(Money.of("115.00")) }
    }

    @Test
    fun `unloading runs in one transaction that rolls back on a domain exception`() {
        val unload = IncomingShipManagementService::class.java.getMethod("unloadIncomingShip", ShipId::class.java)

        val transactional = unload.getAnnotation(Transactional::class.java)

        // jakarta.transaction.Transactional rolls back on every RuntimeException unless told otherwise
        (transactional != null) shouldBe true
        transactional.dontRollbackOn.toList() shouldBe emptyList()
    }

    @Test
    fun `refusing clears the ship first, then writes the voyage home and its Shipping Published, and pays nothing`() {
        val shipSlot = slot<Ship>()
        every { shippingOutboxRepository.broadcastShipping(capture(shipSlot), tortuga) } returns Unit

        val sailsTo = service.refuseIncomingShip(saltyWhiskerId)

        sailsTo shouldBe HarborName("Port Royal")
        verifyOrder {
            shipRepositoryPort.unloadIncomingShip(saltyWhiskerId)
            shippingRepositoryPort.createShipping(any())
            cargoPersistencePort.updateCargoLoad(match { it.cargoLoad == listOf(rum, rum, sugar) })
            shippingRepositoryPort.updateActiveShipping(any())
            shippingOutboxRepository.broadcastShipping(any(), tortuga)
        }
        val released = shipSlot.captured
        released.shippingState() shouldBe ShippingState.SHIPPING
        released.activeShipping!!.destinationHarbor shouldBe HarborName("Port Royal")
        released.loadedCargo shouldBe listOf(rum, rum, sugar)
        verify(exactly = 0) { savingsRepositoryPort.pay(any()) }
        verify(exactly = 0) { stockRepositoryPort.putIntoStock(any(), any()) }
        verify(exactly = 0) { priceRepositoryPort.getPrices() }
    }

    @Test
    fun `refusing a ship not in the fleet returns null and writes nothing`() {
        every { shipRepositoryPort.getShipDetails(saltyWhiskerId) } returns null

        service.refuseIncomingShip(saltyWhiskerId) shouldBe null

        verify(exactly = 0) { shipRepositoryPort.unloadIncomingShip(any()) }
        verify(exactly = 0) { shippingRepositoryPort.createShipping(any()) }
        verify(exactly = 0) { shippingOutboxRepository.broadcastShipping(any(), any()) }
    }

    @Test
    fun `a refusal that loses the ship to a concurrent unloading writes no Shipping and no outbox row`() {
        every { shipRepositoryPort.unloadIncomingShip(saltyWhiskerId) } returns false

        shouldThrow<ShipNotIncomingException> { service.refuseIncomingShip(saltyWhiskerId) }
            .message shouldBe "Salty Whisker is not an Incoming Ship anymore"

        verify(exactly = 0) { shippingRepositoryPort.createShipping(any()) }
        verify(exactly = 0) { cargoPersistencePort.updateCargoLoad(any()) }
        verify(exactly = 0) { shippingRepositoryPort.updateActiveShipping(any()) }
        verify(exactly = 0) { shippingOutboxRepository.broadcastShipping(any(), any()) }
    }

    @Test
    fun `refusing runs in one transaction that rolls back on a domain exception`() {
        val refuse = IncomingShipManagementService::class.java.getMethod("refuseIncomingShip", ShipId::class.java)

        val transactional = refuse.getAnnotation(Transactional::class.java)

        (transactional != null) shouldBe true
        transactional.dontRollbackOn.toList() shouldBe emptyList()
    }

    private fun aShip(cargoAboard: List<Cargo>, incoming: Boolean) = Ship(
        id = saltyWhiskerId,
        name = "Salty Whisker",
        catainId = CatainId(UUID.randomUUID()),
        catainName = "Furry Jones",
        homeHarbor = HarborName("Port Royal"),
        arrivedFrom = HarborName("Tortuga"),
        cargoAboard = cargoAboard,
        incoming = incoming,
    )
}
