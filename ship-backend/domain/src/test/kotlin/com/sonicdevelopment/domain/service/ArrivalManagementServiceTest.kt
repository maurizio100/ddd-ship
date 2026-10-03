package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.fixtures.aCargo
import com.sonicdevelopment.domain.fixtures.aShip
import com.sonicdevelopment.domain.model.Catain
import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.enums.ShippingState
import com.sonicdevelopment.domain.model.values.CatainId
import com.sonicdevelopment.domain.model.values.CatainImageId
import com.sonicdevelopment.domain.model.values.EventId
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.model.values.ShippingId
import com.sonicdevelopment.domain.model.values.ShippingQuote
import com.sonicdevelopment.domain.ports.driven.ArrivalRepositoryPort
import com.sonicdevelopment.domain.ports.driven.CargoQueryPort
import com.sonicdevelopment.domain.ports.driven.CatainRepository
import com.sonicdevelopment.domain.ports.driven.InboxRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort.InitialShipInformation
import com.sonicdevelopment.domain.ports.driven.ShippingOutboxRepository
import com.sonicdevelopment.domain.ports.driven.ShippingRepositoryPort
import com.sonicdevelopment.domain.ports.driven.StockRepositoryPort
import com.sonicdevelopment.domain.ports.driving.shipping.ShipArrivedDTO
import com.sonicdevelopment.domain.ports.driving.shipping.ShippingPublishedDTO
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class ArrivalManagementServiceTest {

    private val portRoyal = HarborName("Port Royal")
    private val tortuga = HarborName("Tortuga")

    private val inbox = mockk<InboxRepositoryPort>()
    private val ships = mockk<ShipRepositoryPort>(relaxed = true)
    private val catains = mockk<CatainRepository>()
    private val cargoQuery = mockk<CargoQueryPort>()
    private val stock = mockk<StockRepositoryPort>(relaxed = true)
    private val outbox = mockk<ShippingOutboxRepository>(relaxed = true)
    private val shippings = mockk<ShippingRepositoryPort>(relaxed = true)
    private val arrivals = mockk<ArrivalRepositoryPort>()

    private val service = ArrivalManagementService(portRoyal, inbox, ships, catains, cargoQuery, stock, outbox, shippings, arrivals)

    /** The Origin side of the Arrival runs at "Tortuga", where the ship was Released. */
    private val tortugaService = ArrivalManagementService(tortuga, inbox, ships, catains, cargoQuery, stock, outbox, shippings, arrivals)

    private val eventId = EventId(UUID.randomUUID())
    private val rum = aCargo(name = "Rum")
    private val silk = aCargo(name = "Silk", weight = 2.0F)
    private val whiskers = Catain(CatainId(UUID.randomUUID()), "Whiskers", CatainImageId("whiskers"))
    private val blackPearl = ShippingPublishedDTO(
        shipId = ShipId(UUID.randomUUID()),
        shipName = "Black Pearl",
        catainId = whiskers.catainId,
        shippingId = ShippingId(UUID.randomUUID()),
        cargoIds = listOf(rum.id, silk.id),
        originHarbor = tortuga,
        destinationHarbor = portRoyal,
    )

    @BeforeEach
    fun knownReferenceData() {
        every { inbox.recordConsumedEvent(eventId) } returns true
        every { ships.getShipDetails(any()) } returns null
        every { arrivals.recordArrival(any(), any()) } returns true
        every { catains.findCatainById(whiskers.catainId) } returns whiskers
        every { cargoQuery.findCargo(rum.id) } returns rum
        every { cargoQuery.findCargo(silk.id) } returns silk
    }

    @Test
    fun `an Arrival unloads every Loaded Cargo into the Stock, takes the ship into the fleet and announces Ship Arrived`() {
        val saved = slot<InitialShipInformation>()
        every { ships.saveNewShip(capture(saved)) } returns Unit
        val announced = slot<Ship>()

        service.receiveShippingPublished(eventId, blackPearl)

        verify(exactly = 1) { stock.putIntoStock(rum.id, 1) }
        verify(exactly = 1) { stock.putIntoStock(silk.id, 1) }
        saved.captured.shipId shouldBe blackPearl.shipId
        saved.captured.shipName shouldBe "Black Pearl"
        saved.captured.catainId shouldBe whiskers.catainId
        verify(exactly = 1) {
            outbox.announceShipArrived(capture(announced), blackPearl.shippingId, tortuga, portRoyal)
        }
        announced.captured.id shouldBe blackPearl.shipId
        announced.captured.shipName shouldBe "Black Pearl"
        announced.captured.catainName shouldBe "Whiskers"
        announced.captured.activeShipping shouldBe null
        announced.captured.loadedCargo.shouldBeEmpty()
        verify(exactly = 1) { arrivals.recordArrival(blackPearl.shippingId, blackPearl.shipId) }
        verify(exactly = 0) { shippings.updateActiveShipping(any()) }
    }

    @Test
    fun `an Arrival takes the ship into the fleet with the Origin Harbor it arrived from`() {
        val saved = mutableListOf<InitialShipInformation>()
        every { ships.saveNewShip(capture(saved)) } returns Unit

        service.receiveShippingPublished(eventId, blackPearl)

        saved.single().arrivedFrom shouldBe tortuga

        // a ship still at sea from here comes back from "Nassau" (arc42 R-10)
        val stillAtSea = aShip(id = blackPearl.shipId, name = "Black Pearl").apply {
            release(ShippingQuote("Fair winds"), HarborName("Nassau"))
        }
        every { ships.getShipDetails(blackPearl.shipId) } returns stillAtSea
        val otherEventId = EventId(UUID.randomUUID())
        every { inbox.recordConsumedEvent(otherEventId) } returns true

        service.receiveShippingPublished(
            otherEventId,
            blackPearl.copy(shippingId = ShippingId(UUID.randomUUID()), originHarbor = HarborName("Nassau")),
        )

        saved.map { it.arrivedFrom } shouldBe listOf(tortuga, HarborName("Nassau"))
    }

    @Test
    fun `a Shipping that has already arrived does not arrive again, even after the ship left the fleet`() {
        // re-published under a new event id; the ship has sailed on, so it is in no fleet here
        every { arrivals.recordArrival(blackPearl.shippingId, blackPearl.shipId) } returns false

        service.receiveShippingPublished(eventId, blackPearl)

        verify(exactly = 1) { inbox.recordConsumedEvent(eventId) }
        verify(exactly = 0) { catains.findCatainById(any()) }
        assertNoArrival()
    }

    @Test
    fun `a ship returning before this Harbor learned of its earlier Arrival elsewhere ends that voyage and stays in the fleet`() {
        // Released here to "Nassau"; its Ship Arrived from there has not been consumed yet
        val stillAtSea = aShip(id = blackPearl.shipId, name = "Black Pearl").apply {
            release(ShippingQuote("Fair winds"), HarborName("Nassau"))
        }
        val earlierVoyage = stillAtSea.activeShipping!!
        every { ships.getShipDetails(blackPearl.shipId) } returns stillAtSea
        val ended = slot<Ship>()
        every { shippings.updateActiveShipping(capture(ended)) } answers {
            ended.captured.activeShipping!!.id shouldBe earlierVoyage.id
            ended.captured.activeShipping!!.shippingState shouldBe ShippingState.DONE
            Unit
        }
        val saved = slot<InitialShipInformation>()
        every { ships.saveNewShip(capture(saved)) } returns Unit

        service.receiveShippingPublished(eventId, blackPearl)

        verify(exactly = 1) { shippings.updateActiveShipping(any()) }
        earlierVoyage.shippingState shouldBe ShippingState.DONE
        verify(exactly = 1) { stock.putIntoStock(rum.id, 1) }
        verify(exactly = 1) { stock.putIntoStock(silk.id, 1) }
        saved.captured.shipId shouldBe blackPearl.shipId
        verify(exactly = 1) { outbox.announceShipArrived(any(), blackPearl.shippingId, tortuga, portRoyal) }
        verify(exactly = 0) { ships.removeFromFleet(any()) }
    }

    @Test
    fun `an already consumed event has no effect`() {
        every { inbox.recordConsumedEvent(eventId) } returns false

        service.receiveShippingPublished(eventId, blackPearl)

        verify(exactly = 0) { ships.getShipDetails(any()) }
        verify(exactly = 0) { catains.findCatainById(any()) }
        verify(exactly = 0) { cargoQuery.findCargo(any()) }
        assertNoArrival()
    }

    @Test
    fun `a Shipping Published for another Harbor or without a Destination Harbor has no effect but is recorded`() {
        val toNassau = blackPearl.copy(destinationHarbor = HarborName("Nassau"))
        val withoutDestination = blackPearl.copy(originHarbor = null, destinationHarbor = null)
        val otherEventId = EventId(UUID.randomUUID())
        every { inbox.recordConsumedEvent(otherEventId) } returns true

        service.receiveShippingPublished(eventId, toNassau)
        service.receiveShippingPublished(otherEventId, withoutDestination)

        verify(exactly = 1) { inbox.recordConsumedEvent(eventId) }
        verify(exactly = 1) { inbox.recordConsumedEvent(otherEventId) }
        assertNoArrival()
    }

    @Test
    fun `a ship already in the fleet and not at sea does not arrive again`() {
        // an Arrival handled before the arrivals were recorded, re-published under a new event id
        every { ships.getShipDetails(blackPearl.shipId) } returns aShip(id = blackPearl.shipId, activeShipping = null)

        service.receiveShippingPublished(eventId, blackPearl)

        every { ships.getShipDetails(blackPearl.shipId) } returns aShip(id = blackPearl.shipId)
        val otherEventId = EventId(UUID.randomUUID())
        every { inbox.recordConsumedEvent(otherEventId) } returns true

        service.receiveShippingPublished(otherEventId, blackPearl)

        verify(exactly = 1) { inbox.recordConsumedEvent(eventId) }
        verify(exactly = 0) { shippings.updateActiveShipping(any()) }
        assertNoArrival()
    }

    @Test
    fun `an Arrival with an unknown Catain or Cargo fails before any change`() {
        every { catains.findCatainById(whiskers.catainId) } returns null

        shouldThrow<IllegalStateException> {
            service.receiveShippingPublished(eventId, blackPearl)
        }.message shouldContain whiskers.catainId.id.toString()
        assertNoArrival()

        every { catains.findCatainById(whiskers.catainId) } returns whiskers
        every { cargoQuery.findCargo(silk.id) } returns null

        shouldThrow<IllegalStateException> {
            service.receiveShippingPublished(eventId, blackPearl)
        }.message shouldContain silk.id.id.toString()
        assertNoArrival()
    }

    @Test
    fun `Ship Arrived for this Harbor sets the Shipping to DONE and removes the ship from the fleet`() {
        val ship = aShipAtSea()
        every { ships.getShipDetails(ship.id) } returns ship
        val updated = slot<Ship>()
        every { shippings.updateActiveShipping(capture(updated)) } answers {
            updated.captured.activeShipping!!.shippingState shouldBe ShippingState.DONE
            Unit
        }

        tortugaService.receiveShipArrived(eventId, shipArrived(ship))

        verifyOrder {
            inbox.recordConsumedEvent(eventId)
            shippings.updateActiveShipping(ship)
            ships.removeFromFleet(ship.id)
        }
        updated.captured.activeShipping!!.id shouldBe ship.activeShipping!!.id
        ship.shippingState() shouldBe ShippingState.DONE
    }

    @Test
    fun `an already consumed Ship Arrived has no effect`() {
        val ship = aShipAtSea()
        every { ships.getShipDetails(ship.id) } returns ship
        every { inbox.recordConsumedEvent(eventId) } returns false

        tortugaService.receiveShipArrived(eventId, shipArrived(ship))

        verify(exactly = 0) { ships.getShipDetails(any()) }
        ship.shippingState() shouldBe ShippingState.SHIPPING
        assertVoyageNotEnded()
    }

    @Test
    fun `Ship Arrived for another Origin Harbor or without one has no effect but is recorded`() {
        val ship = aShipAtSea()
        every { ships.getShipDetails(ship.id) } returns ship
        val otherEventId = EventId(UUID.randomUUID())
        every { inbox.recordConsumedEvent(otherEventId) } returns true

        tortugaService.receiveShipArrived(eventId, shipArrived(ship).copy(originHarbor = HarborName("Nassau")))
        tortugaService.receiveShipArrived(otherEventId, shipArrived(ship).copy(originHarbor = null))

        verify(exactly = 1) { inbox.recordConsumedEvent(eventId) }
        verify(exactly = 1) { inbox.recordConsumedEvent(otherEventId) }
        ship.shippingState() shouldBe ShippingState.SHIPPING
        assertVoyageNotEnded()
    }

    @Test
    fun `Ship Arrived for a ship not in the fleet has no effect`() {
        val ship = aShipAtSea()
        every { ships.getShipDetails(ship.id) } returns null

        tortugaService.receiveShipArrived(eventId, shipArrived(ship))

        verify(exactly = 1) { inbox.recordConsumedEvent(eventId) }
        assertVoyageNotEnded()
    }

    @Test
    fun `Ship Arrived for a Shipping that is not the ship's voyage at sea has no effect`() {
        // the ship sails again on a newer Shipping
        val sailingAgain = aShipAtSea()
        every { ships.getShipDetails(sailingAgain.id) } returns sailingAgain
        val earlierVoyage = ShippingId(UUID.randomUUID())

        tortugaService.receiveShipArrived(eventId, shipArrived(sailingAgain).copy(shippingId = earlierVoyage))

        sailingAgain.shippingState() shouldBe ShippingState.SHIPPING
        assertVoyageNotEnded()

        // that Shipping is already DONE
        val alreadyDone = aShipAtSea()
        alreadyDone.endShipping(alreadyDone.activeShipping!!.id)
        every { ships.getShipDetails(alreadyDone.id) } returns alreadyDone
        val otherEventId = EventId(UUID.randomUUID())
        every { inbox.recordConsumedEvent(otherEventId) } returns true

        tortugaService.receiveShipArrived(otherEventId, shipArrived(alreadyDone))

        alreadyDone.shippingState() shouldBe ShippingState.DONE
        assertVoyageNotEnded()
    }

    private fun aShipAtSea(): Ship = aShip(name = "Black Pearl").apply {
        release(ShippingQuote("Fair winds"), portRoyal)
    }

    private fun shipArrived(ship: Ship) = ShipArrivedDTO(
        shipId = ship.id,
        shippingId = ship.activeShipping!!.id,
        originHarbor = tortuga,
        destinationHarbor = portRoyal,
    )

    private fun assertVoyageNotEnded() {
        verify(exactly = 0) { shippings.updateActiveShipping(any()) }
        verify(exactly = 0) { ships.removeFromFleet(any()) }
    }

    private fun assertNoArrival() {
        verify(exactly = 0) { stock.putIntoStock(any(), any()) }
        verify(exactly = 0) { ships.saveNewShip(any()) }
        verify(exactly = 0) { outbox.announceShipArrived(any(), any(), any(), any()) }
    }
}
