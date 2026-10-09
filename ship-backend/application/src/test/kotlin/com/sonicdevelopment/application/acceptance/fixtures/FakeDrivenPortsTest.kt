package com.sonicdevelopment.application.acceptance.fixtures

import com.sonicdevelopment.domain.model.values.CargoId
import com.sonicdevelopment.domain.model.values.EventId
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.model.values.ShippingId
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
}
