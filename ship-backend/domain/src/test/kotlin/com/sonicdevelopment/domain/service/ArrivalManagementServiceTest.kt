package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.fixtures.aCargo
import com.sonicdevelopment.domain.fixtures.aShip
import com.sonicdevelopment.domain.model.Catain
import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.values.CatainId
import com.sonicdevelopment.domain.model.values.CatainImageId
import com.sonicdevelopment.domain.model.values.EventId
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.model.values.ShippingId
import com.sonicdevelopment.domain.ports.driven.CargoQueryPort
import com.sonicdevelopment.domain.ports.driven.CatainRepository
import com.sonicdevelopment.domain.ports.driven.InboxRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort.InitialShipInformation
import com.sonicdevelopment.domain.ports.driven.ShippingOutboxRepository
import com.sonicdevelopment.domain.ports.driven.StockRepositoryPort
import com.sonicdevelopment.domain.ports.driving.shipping.ShippingPublishedDTO
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
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

    private val service = ArrivalManagementService(portRoyal, inbox, ships, catains, cargoQuery, stock, outbox)

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
    fun `a ship already in the fleet does not arrive again`() {
        every { ships.getShipDetails(blackPearl.shipId) } returns aShip(id = blackPearl.shipId, activeShipping = null)

        service.receiveShippingPublished(eventId, blackPearl)

        verify(exactly = 1) { inbox.recordConsumedEvent(eventId) }
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

    private fun assertNoArrival() {
        verify(exactly = 0) { stock.putIntoStock(any(), any()) }
        verify(exactly = 0) { ships.saveNewShip(any()) }
        verify(exactly = 0) { outbox.announceShipArrived(any(), any(), any(), any()) }
    }
}
