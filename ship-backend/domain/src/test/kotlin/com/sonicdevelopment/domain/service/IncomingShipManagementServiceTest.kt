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
import com.sonicdevelopment.domain.ports.driven.PriceRepositoryPort
import com.sonicdevelopment.domain.ports.driven.SavingsRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort
import com.sonicdevelopment.domain.ports.driven.StockRepositoryPort
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
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

    private val service = IncomingShipManagementService(
        shipRepositoryPort, priceRepositoryPort, savingsRepositoryPort, stockRepositoryPort
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

    private fun aShip(cargoAboard: List<Cargo>, incoming: Boolean) = Ship(
        id = saltyWhiskerId,
        name = "Salty Whisker",
        catainId = CatainId(UUID.randomUUID()),
        catainName = "Furry Jones",
        arrivedFrom = HarborName("Tortuga"),
        cargoAboard = cargoAboard,
        incoming = incoming,
    )
}
