package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.fixtures.aCargo
import com.sonicdevelopment.domain.model.Cargo
import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.Shipping
import com.sonicdevelopment.domain.model.enums.ShippingState
import com.sonicdevelopment.domain.model.values.CatainId
import com.sonicdevelopment.domain.model.values.EventId
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.model.values.ShippingId
import com.sonicdevelopment.domain.model.values.ShippingQuote
import com.sonicdevelopment.domain.ports.driven.CargoPersistencePort
import com.sonicdevelopment.domain.ports.driven.FleetEventsPort
import com.sonicdevelopment.domain.ports.driven.InboxRepositoryPort
import com.sonicdevelopment.domain.ports.driven.QuoteRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShippingOutboxRepository
import com.sonicdevelopment.domain.ports.driven.ShippingRepositoryPort
import com.sonicdevelopment.domain.ports.driving.shipping.ShipImprisonedDTO
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

class ImprisonmentManagementServiceTest {

    private val portRoyal = HarborName("Port Royal")
    private val tortuga = HarborName("Tortuga")

    private val inbox = mockk<InboxRepositoryPort>()
    private val ships = mockk<ShipRepositoryPort>(relaxed = true)
    private val shippings = mockk<ShippingRepositoryPort>(relaxed = true)
    private val cargoPersistence = mockk<CargoPersistencePort>()
    private val quotes = mockk<QuoteRepositoryPort>()
    private val outbox = mockk<ShippingOutboxRepository>(relaxed = true)
    private val fleetEvents = mockk<FleetEventsPort>(relaxed = true)

    private val atPortRoyal = ImprisonmentManagementService(portRoyal, inbox, ships, shippings, cargoPersistence, quotes, outbox, fleetEvents)
    private val atTortuga = ImprisonmentManagementService(tortuga, inbox, ships, shippings, cargoPersistence, quotes, outbox, fleetEvents)

    private val eventId = EventId(UUID.randomUUID())
    private val saltyWhiskerId = ShipId(UUID.randomUUID())
    private val rum = aCargo(name = "Rum")
    private val heldAtPortRoyal = ShipImprisonedDTO(saltyWhiskerId, "Salty Whisker", portRoyal, tortuga)
    private val heldAtTortuga = heldAtPortRoyal.copy(holdingHarbor = tortuga)

    @BeforeEach
    fun saltyWhiskerArrivedWithTwoRum() {
        every { inbox.recordConsumedEvent(any()) } returns true
        every { ships.lockShip(saltyWhiskerId) } returns aShip(cargoAboard = listOf(rum, rum), incoming = true)
        every { cargoPersistence.updateCargoLoad(any()) } answers { firstArg() }
        every { quotes.getQuoteForSailorsCode(any()) } returns ShippingQuote("Fair winds")
    }

    @Test
    fun `away from its Home Harbor the ship is confiscated, then Released home imprisoned, in order`() {
        val broadcast = slot<Ship>()
        every { outbox.broadcastShipping(capture(broadcast), portRoyal) } returns Unit

        atPortRoyal.receiveShipImprisoned(eventId, heldAtPortRoyal)

        verifyOrder {
            inbox.recordConsumedEvent(eventId)
            ships.lockShip(saltyWhiskerId)
            ships.confiscate(saltyWhiskerId)
            shippings.createShipping(any())
            cargoPersistence.updateCargoLoad(match { it.cargoLoad.isEmpty() })
            shippings.updateActiveShipping(any())
            outbox.broadcastShipping(any(), portRoyal)
            fleetEvents.announceShipImprisoned(any(), sailsHome = true)
        }
        broadcast.captured.isImprisoned shouldBe true
        broadcast.captured.loadedCargo shouldBe emptyList()
        broadcast.captured.shippingState() shouldBe ShippingState.SHIPPING
        broadcast.captured.activeShipping!!.destinationHarbor shouldBe tortuga
    }

    @Test
    fun `away from its Home Harbor a PREPARING Shipping is reused, not created again`() {
        every { ships.lockShip(saltyWhiskerId) } returns aShip(
            activeShipping = Shipping(ShippingId(UUID.randomUUID())), loadedCargo = listOf(rum),
        )

        atPortRoyal.receiveShipImprisoned(eventId, heldAtPortRoyal)

        verify(exactly = 0) { shippings.createShipping(any()) }
        verifyOrder {
            ships.confiscate(saltyWhiskerId)
            cargoPersistence.updateCargoLoad(match { it.cargoLoad.isEmpty() })
            shippings.updateActiveShipping(any())
            outbox.broadcastShipping(any(), portRoyal)
        }
        verify(exactly = 1) { cargoPersistence.updateCargoLoad(any()) }
    }

    @Test
    fun `at its Home Harbor the ship is only confiscated and the fleet is told`() {
        atTortuga.receiveShipImprisoned(eventId, heldAtTortuga)

        verifyOrder {
            ships.lockShip(saltyWhiskerId)
            ships.confiscate(saltyWhiskerId)
            fleetEvents.announceShipImprisoned(match { it.isImprisoned }, sailsHome = false)
        }
        verify(exactly = 0) { shippings.createShipping(any()) }
        verify(exactly = 0) { cargoPersistence.updateCargoLoad(any()) }
        verify(exactly = 0) { shippings.updateActiveShipping(any()) }
        verify(exactly = 0) { outbox.broadcastShipping(any(), any()) }
    }

    @Test
    fun `a redelivered event writes nothing`() {
        every { inbox.recordConsumedEvent(eventId) } returns false

        atPortRoyal.receiveShipImprisoned(eventId, heldAtPortRoyal)

        verify(exactly = 0) { ships.lockShip(any()) }
        nothingBeyondTheInboxWasWritten()
    }

    @Test
    fun `an imprisonment held at another Harbor is recorded and writes nothing else`() {
        atTortuga.receiveShipImprisoned(eventId, heldAtPortRoyal)

        verify(exactly = 1) { inbox.recordConsumedEvent(eventId) }
        verify(exactly = 0) { ships.lockShip(any()) }
        nothingBeyondTheInboxWasWritten()
    }

    @Test
    fun `a ship not in the fleet escapes`() {
        every { ships.lockShip(saltyWhiskerId) } returns null

        atPortRoyal.receiveShipImprisoned(eventId, heldAtPortRoyal)

        verify(exactly = 1) { inbox.recordConsumedEvent(eventId) }
        nothingBeyondTheInboxWasWritten()
    }

    @Test
    fun `a ship at sea escapes`() {
        val atSea = aShip(activeShipping = Shipping(ShippingId(UUID.randomUUID())), loadedCargo = listOf(rum))
        atSea.release(ShippingQuote("Fair winds"), HarborName("Isla de Muerta"))
        every { ships.lockShip(saltyWhiskerId) } returns atSea

        atPortRoyal.receiveShipImprisoned(eventId, heldAtPortRoyal)

        nothingBeyondTheInboxWasWritten()
    }

    @Test
    fun `a second imprisonment under a new event id after the ship was sent home changes nothing`() {
        val sentHome = aShip(cargoAboard = listOf(rum), incoming = true)
        sentHome.confiscate(portRoyal)
        sentHome.sailHomeImprisoned(ShippingQuote("Fair winds"))
        every { ships.lockShip(saltyWhiskerId) } returns sentHome

        atPortRoyal.receiveShipImprisoned(EventId(UUID.randomUUID()), heldAtPortRoyal)

        nothingBeyondTheInboxWasWritten()
    }

    @Test
    fun `an imprisonment runs in one transaction that rolls back on any exception`() {
        val receive = ImprisonmentManagementService::class.java.getMethod(
            "receiveShipImprisoned", EventId::class.java, ShipImprisonedDTO::class.java,
        )

        val transactional = receive.getAnnotation(Transactional::class.java)

        (transactional != null) shouldBe true
        transactional.dontRollbackOn.toList() shouldBe emptyList()
    }

    private fun nothingBeyondTheInboxWasWritten() {
        verify(exactly = 0) { ships.confiscate(any()) }
        verify(exactly = 0) { shippings.createShipping(any()) }
        verify(exactly = 0) { cargoPersistence.updateCargoLoad(any()) }
        verify(exactly = 0) { shippings.updateActiveShipping(any()) }
        verify(exactly = 0) { outbox.broadcastShipping(any(), any()) }
        verify(exactly = 0) { fleetEvents.announceShipImprisoned(any(), any()) }
    }

    private fun aShip(
        cargoAboard: List<Cargo> = emptyList(),
        incoming: Boolean = false,
        activeShipping: Shipping? = null,
        loadedCargo: List<Cargo> = emptyList(),
    ) = Ship(
        id = saltyWhiskerId,
        name = "Salty Whisker",
        catainId = CatainId(UUID.randomUUID()),
        catainName = "Furry Jones",
        homeHarbor = tortuga,
        arrivedFrom = tortuga,
        activeShipping = activeShipping,
        cargoLoad = loadedCargo.toMutableList(),
        cargoAboard = cargoAboard,
        incoming = incoming,
    )
}
