package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.exception.CargoHasNoPriceException
import com.sonicdevelopment.domain.exception.SavingsDoNotCoverException
import com.sonicdevelopment.domain.fixtures.aCargo
import com.sonicdevelopment.domain.model.values.CargoId
import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.domain.ports.driven.CargoQueryPort
import com.sonicdevelopment.domain.ports.driven.PriceRepositoryPort
import com.sonicdevelopment.domain.ports.driven.SavingsRepositoryPort
import com.sonicdevelopment.domain.ports.driven.StockRepositoryPort
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class MarketServiceTest {

    private val cargoQueryPort = mockk<CargoQueryPort>()
    private val priceRepositoryPort = mockk<PriceRepositoryPort>()
    private val savingsRepositoryPort = mockk<SavingsRepositoryPort>()
    private val stockRepositoryPort = mockk<StockRepositoryPort>(relaxed = true)

    private val service = MarketService(cargoQueryPort, priceRepositoryPort, savingsRepositoryPort, stockRepositoryPort)

    private val ale = aCargo(name = "Ale")

    @BeforeEach
    fun aleCostsFiftyDollars() {
        every { cargoQueryPort.findCargo(ale.id) } returns ale
        every { priceRepositoryPort.getPrices() } returns mapOf(ale.id to Money.of("50.00"))
    }

    @Test
    fun `buying pays Price times quantity and then puts the quantity into the Stock`() {
        every { savingsRepositoryPort.pay(Money.of("100.00")) } returns true

        val cost = service.buyCargo(ale.id, 2)

        cost shouldBe Money.of("100.00")
        verifyOrder {
            savingsRepositoryPort.pay(Money.of("100.00"))
            stockRepositoryPort.putIntoStock(ale.id, 2)
        }
    }

    @Test
    fun `a purchase the Savings cannot cover is refused and puts nothing into the Stock`() {
        every { savingsRepositoryPort.pay(Money.of("100.00")) } returns false

        val refusal = shouldThrow<SavingsDoNotCoverException> { service.buyCargo(ale.id, 2) }

        refusal.cost shouldBe Money.of("100.00")
        refusal.message shouldBe "The Savings do not cover 100.00 $"
        verify(exactly = 0) { stockRepositoryPort.putIntoStock(any(), any()) }
    }

    @Test
    fun `buying an unknown Cargo returns null and neither pays nor stocks`() {
        val unknown = CargoId(UUID.randomUUID())
        every { cargoQueryPort.findCargo(unknown) } returns null

        service.buyCargo(unknown, 2) shouldBe null

        verify(exactly = 0) { savingsRepositoryPort.pay(any()) }
        verify(exactly = 0) { stockRepositoryPort.putIntoStock(any(), any()) }
    }

    @Test
    fun `a Cargo without a Price cannot be bought and nothing changes`() {
        every { priceRepositoryPort.getPrices() } returns emptyMap()

        val refusal = shouldThrow<CargoHasNoPriceException> { service.buyCargo(ale.id, 2) }

        refusal.message shouldBe "Ale has no Price yet"
        verify(exactly = 0) { savingsRepositoryPort.pay(any()) }
        verify(exactly = 0) { stockRepositoryPort.putIntoStock(any(), any()) }
    }

    @Test
    fun `a quantity below 1 is rejected before anything changes`() {
        shouldThrow<IllegalArgumentException> { service.buyCargo(ale.id, 0) }

        verify(exactly = 0) { savingsRepositoryPort.pay(any()) }
        verify(exactly = 0) { stockRepositoryPort.putIntoStock(any(), any()) }
    }
}
