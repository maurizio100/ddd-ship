package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.fixtures.aCargo
import com.sonicdevelopment.domain.model.values.EventId
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.domain.ports.driven.CargoQueryPort
import com.sonicdevelopment.domain.ports.driven.HarborOutboxRepositoryPort
import com.sonicdevelopment.domain.ports.driven.InboxRepositoryPort
import com.sonicdevelopment.domain.ports.driven.KnownHarborRepositoryPort
import com.sonicdevelopment.domain.ports.driven.PriceRepositoryPort
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.Test
import java.util.*

class HarborManagementServiceTest {

    private val currentHarbor = HarborName("Tortuga")
    private val knownHarbors = mockk<KnownHarborRepositoryPort>(relaxed = true)
    private val harborOutbox = mockk<HarborOutboxRepositoryPort>(relaxed = true)
    private val inbox = mockk<InboxRepositoryPort>()
    private val cargoQueryPort = mockk<CargoQueryPort>()
    private val prices = mockk<PriceRepositoryPort>()
    private val priceRoll = mockk<PriceRoll>()

    private val service = HarborManagementService(
        currentHarbor, knownHarbors, harborOutbox, inbox, cargoQueryPort, prices, priceRoll
    )

    @Test
    fun `learns about another Harbor`() {
        val eventId = EventId(UUID.randomUUID())
        every { inbox.recordConsumedEvent(eventId) } returns true

        service.learnAboutHarbor(eventId, HarborName("Port Royal"))

        verifyOrder {
            inbox.recordConsumedEvent(eventId)
            knownHarbors.rememberHarbor(HarborName("Port Royal"))
        }
    }

    @Test
    fun `does not remember its own Harbor Name`() {
        val eventId = EventId(UUID.randomUUID())
        every { inbox.recordConsumedEvent(eventId) } returns true

        service.learnAboutHarbor(eventId, currentHarbor)

        verify { inbox.recordConsumedEvent(eventId) }
        verify(exactly = 0) { knownHarbors.rememberHarbor(any()) }
    }

    @Test
    fun `ignores an already consumed Harbor Opened`() {
        val eventId = EventId(UUID.randomUUID())
        every { inbox.recordConsumedEvent(eventId) } returns false

        service.learnAboutHarbor(eventId, HarborName("Port Royal"))

        verify(exactly = 0) { knownHarbors.rememberHarbor(any()) }
    }

    @Test
    fun `opening publishes Harbor Opened with the current Harbor Name`() {
        every { cargoQueryPort.findAllCargo() } returns emptyList()
        every { prices.getPrices() } returns emptyMap()

        service.openHarbor()

        verify(exactly = 1) { harborOutbox.publishHarborOpened(currentHarbor) }
    }

    @Test
    fun `the first opening rolls a Price for every catalog Cargo`() {
        val rum = aCargo(name = "Rum")
        val ale = aCargo(name = "Ale")
        every { cargoQueryPort.findAllCargo() } returns listOf(rum, ale)
        every { prices.getPrices() } returns emptyMap()
        every { priceRoll.roll() } returnsMany listOf(Money.dollars(42), Money.dollars(57))
        every { prices.rememberPrice(any(), any()) } returns true

        service.openHarbor()

        verifyOrder {
            prices.rememberPrice(rum.id, Money.dollars(42))
            prices.rememberPrice(ale.id, Money.dollars(57))
            harborOutbox.publishHarborOpened(currentHarbor)
        }
        verify(exactly = 2) { prices.rememberPrice(any(), any()) }
    }

    @Test
    fun `opening again rolls no Price for a Cargo that has one`() {
        val rum = aCargo(name = "Rum")
        val ale = aCargo(name = "Ale")
        every { cargoQueryPort.findAllCargo() } returns listOf(rum, ale)
        every { prices.getPrices() } returns mapOf(rum.id to Money.dollars(42))
        every { priceRoll.roll() } returns Money.dollars(33)
        every { prices.rememberPrice(any(), any()) } returns true

        service.openHarbor()

        verify(exactly = 0) { prices.rememberPrice(rum.id, any()) }
        verify(exactly = 1) { prices.rememberPrice(ale.id, Money.dollars(33)) }
        verify(exactly = 1) { priceRoll.roll() }
    }
}
