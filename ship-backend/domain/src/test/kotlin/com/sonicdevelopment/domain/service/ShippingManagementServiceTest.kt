package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.exception.ShippingNotPreparingException
import com.sonicdevelopment.domain.exception.UnknownHarborException
import com.sonicdevelopment.domain.fixtures.aShip
import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.enums.ShippingState
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.ShippingQuote
import com.sonicdevelopment.domain.ports.driven.KnownHarborRepositoryPort
import com.sonicdevelopment.domain.ports.driven.QuoteRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShippingOutboxRepository
import com.sonicdevelopment.domain.ports.driven.ShippingRepositoryPort
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class ShippingManagementServiceTest {

    private val shipRepositoryPort = mockk<ShipRepositoryPort>()
    private val shippingRepositoryPort = mockk<ShippingRepositoryPort>(relaxed = true)
    private val quoteRepositoryPort = mockk<QuoteRepositoryPort>()
    private val shippingOutboxRepository = mockk<ShippingOutboxRepository>(relaxed = true)
    private val knownHarborRepositoryPort = mockk<KnownHarborRepositoryPort>()

    private val service = ShippingManagementService(
        shipRepositoryPort,
        shippingRepositoryPort,
        quoteRepositoryPort,
        shippingOutboxRepository,
        HarborName("Tortuga"),
        knownHarborRepositoryPort,
    )

    init {
        every { quoteRepositoryPort.getQuoteForSailorsCode(any()) } returns ShippingQuote("Fair winds")
    }

    @Test
    fun `releasing to a Known Harbor sets the Destination Harbor and publishes with the current Harbor as Origin`() {
        val ship = givenShip(aShip())
        givenKnownHarbors("Nassau", "Port Royal")

        val released = service.releaseShipping(ship.id, HarborName("Port Royal"))

        released!!.destinationHarbor shouldBe HarborName("Port Royal")
        ship.activeShipping!!.destinationHarbor shouldBe HarborName("Port Royal")
        ship.shippingState() shouldBe ShippingState.SHIPPING
        verify(exactly = 1) { shippingRepositoryPort.updateActiveShipping(ship) }
        verify(exactly = 1) { shippingOutboxRepository.broadcastShipping(ship, HarborName("Tortuga")) }
    }

    @Test
    fun `releasing to an unknown Harbor throws UnknownHarborException and writes nothing`() {
        val ship = givenShip(aShip())
        givenKnownHarbors("Port Royal")

        val rejection = shouldThrow<UnknownHarborException> {
            service.releaseShipping(ship.id, HarborName("Atlantis"))
        }

        rejection.message shouldBe "Atlantis is not a Known Harbor"
        ship.shippingState() shouldBe ShippingState.PREPARING
        ship.activeShipping!!.destinationHarbor shouldBe null
        verify(exactly = 0) { shippingRepositoryPort.updateActiveShipping(any()) }
        verify(exactly = 0) { shippingOutboxRepository.broadcastShipping(any(), any()) }
    }

    @Test
    fun `releasing with no Known Harbors is rejected`() {
        val ship = givenShip(aShip())
        givenKnownHarbors()

        shouldThrow<UnknownHarborException> { service.releaseShipping(ship.id, HarborName("Port Royal")) }

        ship.shippingState() shouldBe ShippingState.PREPARING
        verify(exactly = 0) { shippingRepositoryPort.updateActiveShipping(any()) }
        verify(exactly = 0) { shippingOutboxRepository.broadcastShipping(any(), any()) }
    }

    @Test
    fun `releasing a ship that is already at sea throws ShippingNotPreparingException and writes nothing`() {
        val ship = givenShip(aShip(name = "Black Pearl"))
        givenKnownHarbors("Nassau", "Port Royal")
        ship.release(ShippingQuote("Fair winds"), HarborName("Port Royal"))

        val rejection = shouldThrow<ShippingNotPreparingException> {
            service.releaseShipping(ship.id, HarborName("Nassau"))
        }

        rejection.message shouldBe "Black Pearl is not being prepared"
        ship.activeShipping!!.destinationHarbor shouldBe HarborName("Port Royal")
        verify(exactly = 0) { shippingRepositoryPort.updateActiveShipping(any()) }
        verify(exactly = 0) { shippingOutboxRepository.broadcastShipping(any(), any()) }
    }

    private fun givenShip(ship: Ship): Ship {
        every { shipRepositoryPort.getShipDetails(ship.id) } returns ship
        return ship
    }

    private fun givenKnownHarbors(vararg names: String) {
        every { knownHarborRepositoryPort.getKnownHarbors() } returns names.map { HarborName(it) }
    }
}
