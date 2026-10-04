package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.exception.CargoOutOfStockException
import com.sonicdevelopment.domain.exception.ItemAlreadyLoadedException
import com.sonicdevelopment.domain.exception.ShipTooHeavyException
import com.sonicdevelopment.domain.fixtures.aCargo
import com.sonicdevelopment.domain.fixtures.aShip
import com.sonicdevelopment.domain.model.Cargo
import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.ports.driven.CargoPersistencePort
import com.sonicdevelopment.domain.ports.driven.CargoQueryPort
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort
import com.sonicdevelopment.domain.ports.driven.StockRepositoryPort
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.Test

class CargoLoadManagementServiceTest {

    private val shipRepositoryPort = mockk<ShipRepositoryPort>()
    private val cargoPersistencePort = mockk<CargoPersistencePort>(relaxed = true)
    private val cargoQueryPort = mockk<CargoQueryPort>()
    private val stockRepositoryPort = mockk<StockRepositoryPort>(relaxed = true)

    private val service = CargoLoadManagementService(
        shipRepositoryPort, cargoPersistencePort, cargoQueryPort, stockRepositoryPort
    )

    private val rum = aCargo(name = "Rum", weight = 5.5F)

    @Test
    fun `loading takes one Cargo out of the Stock`() {
        val ship = givenShip(aShip())
        givenCargo(rum)
        every { stockRepositoryPort.takeOneFromStock(rum.id) } returns true

        val loaded = service.addCargo(ship.id, rum.id)

        loaded!!.cargo.map { it.name } shouldBe listOf("Rum")
        verifyOrder {
            stockRepositoryPort.takeOneFromStock(rum.id)
            cargoPersistencePort.updateCargoLoad(any())
        }
    }

    @Test
    fun `loading Cargo that is out of Stock is rejected and the cargo load is not saved`() {
        val ship = givenShip(aShip())
        givenCargo(rum)
        every { stockRepositoryPort.takeOneFromStock(rum.id) } returns false

        val rejection = shouldThrow<CargoOutOfStockException> { service.addCargo(ship.id, rum.id) }

        rejection.message shouldBe "Rum is out of Stock"
        verify(exactly = 0) { cargoPersistencePort.updateCargoLoad(any()) }
    }

    @Test
    fun `a load too heavy for the ship is rejected without touching the Stock`() {
        val ship = givenShip(aShip(loadedCargo = listOf(aCargo(name = "Planks", weight = 14.0F))))
        givenCargo(rum)

        shouldThrow<ShipTooHeavyException> { service.addCargo(ship.id, rum.id) }

        verify(exactly = 0) { stockRepositoryPort.takeOneFromStock(any()) }
        verify(exactly = 0) { cargoPersistencePort.updateCargoLoad(any()) }
    }

    @Test
    fun `loading Cargo already on board is rejected without touching the Stock`() {
        val ship = givenShip(aShip(loadedCargo = listOf(rum)))
        givenCargo(rum)

        shouldThrow<ItemAlreadyLoadedException> { service.addCargo(ship.id, rum.id) }

        verify(exactly = 0) { stockRepositoryPort.takeOneFromStock(any()) }
        verify(exactly = 0) { cargoPersistencePort.updateCargoLoad(any()) }
    }

    @Test
    fun `unloading loaded Cargo puts one back into the Stock`() {
        val ship = givenShip(aShip(loadedCargo = listOf(rum)))
        givenCargo(rum)

        val unloaded = service.removeCargo(ship.id, rum.id)

        unloaded!!.cargo shouldBe emptyList()
        verify(exactly = 1) { stockRepositoryPort.putIntoStock(rum.id, 1) }
        verify(exactly = 1) { cargoPersistencePort.updateCargoLoad(any()) }
    }

    @Test
    fun `unloading Cargo that is not loaded leaves the Stock unchanged`() {
        val ship = givenShip(aShip())
        givenCargo(rum)

        service.removeCargo(ship.id, rum.id)

        verify(exactly = 0) { stockRepositoryPort.putIntoStock(any(), any()) }
    }

    private fun givenShip(ship: Ship): Ship {
        every { shipRepositoryPort.getShipDetails(ship.id) } returns ship
        return ship
    }

    private fun givenCargo(cargo: Cargo) {
        every { cargoQueryPort.findCargo(cargo.id) } returns cargo
    }
}
