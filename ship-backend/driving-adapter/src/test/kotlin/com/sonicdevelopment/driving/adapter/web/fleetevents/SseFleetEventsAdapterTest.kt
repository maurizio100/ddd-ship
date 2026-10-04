package com.sonicdevelopment.driving.adapter.web.fleetevents

import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.values.CatainId
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.ShipId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.transaction.support.TransactionSynchronizationUtils
import java.util.UUID

class SseFleetEventsAdapterTest {

    private val emitters = mockk<FleetEventEmitters>(relaxed = true)
    private val adapter = SseFleetEventsAdapter(emitters)

    private val blackPearl = Ship(
        id = ShipId(UUID.randomUUID()),
        name = "Black Pearl",
        catainId = CatainId(UUID.randomUUID()),
        catainName = "Whiskers",
    )
    private val arrived = ShipArrivedFleetEvent(blackPearl.id.id, "Black Pearl", "Tortuga")

    @AfterEach
    fun clearSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization()
        }
    }

    @Test
    fun `outside a transaction an arrival is pushed at once`() {
        adapter.announceShipArrived(blackPearl, HarborName("Tortuga"))

        verify(exactly = 1) { emitters.broadcast("ship-arrived", arrived) }
    }

    @Test
    fun `a ship that left is pushed with its Destination Harbor`() {
        adapter.announceShipLeft(blackPearl, HarborName("Port Royal"))
        adapter.announceShipLeft(blackPearl, null)

        verify(exactly = 1) {
            emitters.broadcast("ship-left", ShipLeftFleetEvent(blackPearl.id.id, "Black Pearl", "Port Royal"))
        }
        verify(exactly = 1) {
            emitters.broadcast("ship-left", ShipLeftFleetEvent(blackPearl.id.id, "Black Pearl", null))
        }
    }

    @Test
    fun `inside a transaction nothing is pushed until it commits`() {
        TransactionSynchronizationManager.initSynchronization()

        adapter.announceShipArrived(blackPearl, HarborName("Tortuga"))
        adapter.announceShipLeft(blackPearl, HarborName("Port Royal"))

        verify(exactly = 0) { emitters.broadcast(any(), any()) }

        commit()

        verify(exactly = 1) { emitters.broadcast("ship-arrived", arrived) }
        verify(exactly = 1) { emitters.broadcast("ship-left", any()) }
    }

    @Test
    fun `nothing is pushed when the transaction rolls back`() {
        TransactionSynchronizationManager.initSynchronization()

        adapter.announceShipArrived(blackPearl, HarborName("Tortuga"))
        val synchronizations = TransactionSynchronizationManager.getSynchronizations()
        TransactionSynchronizationManager.clearSynchronization()
        TransactionSynchronizationUtils.invokeAfterCompletion(
            synchronizations, TransactionSynchronization.STATUS_ROLLED_BACK
        )

        verify(exactly = 0) { emitters.broadcast(any(), any()) }
    }

    @Test
    fun `a failing push never reaches the committed transaction's caller`() {
        every { emitters.broadcast(any(), any()) } throws IllegalStateException("emitter gone")

        assertDoesNotThrow { adapter.announceShipArrived(blackPearl, HarborName("Tortuga")) }

        TransactionSynchronizationManager.initSynchronization()
        adapter.announceShipLeft(blackPearl, HarborName("Port Royal"))
        assertDoesNotThrow { commit() }
    }

    private fun commit() {
        val synchronizations = TransactionSynchronizationManager.getSynchronizations()
        TransactionSynchronizationManager.clearSynchronization()
        TransactionSynchronizationUtils.invokeAfterCommit(synchronizations)
        TransactionSynchronizationUtils.invokeAfterCompletion(
            synchronizations, TransactionSynchronization.STATUS_COMMITTED
        )
    }
}
