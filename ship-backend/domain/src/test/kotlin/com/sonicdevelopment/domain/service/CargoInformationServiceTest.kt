package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.fixtures.aCargo
import com.sonicdevelopment.domain.ports.driven.CargoQueryPort
import com.sonicdevelopment.domain.ports.driven.StockRepositoryPort
import com.sonicdevelopment.domain.ports.driving.cargo.AvailableCargoDTO
import com.sonicdevelopment.domain.ports.driving.cargo.StockedCargoDTO
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class CargoInformationServiceTest {

    private val cargoQueryPort = mockk<CargoQueryPort>()
    private val stockRepositoryPort = mockk<StockRepositoryPort>()

    private val service = CargoInformationService(cargoQueryPort, stockRepositoryPort)

    @Test
    fun `Available Cargo is the catalog Cargo with Stock above 0, with its Stock`() {
        val ale = aCargo(name = "Ale", weight = 2.0F)
        val rum = aCargo(name = "Rum", weight = 5.5F)
        val silk = aCargo(name = "Silk", weight = 2.0F)
        val wine = aCargo(name = "Wine", weight = 3.3F)
        every { cargoQueryPort.findAllCargo() } returns listOf(ale, rum, silk, wine)
        every { stockRepositoryPort.getStock() } returns mapOf(rum.id to 2, ale.id to 3, silk.id to 0)

        service.getAvailableCargo() shouldBe listOf(
            AvailableCargoDTO(id = ale.id, name = "Ale", weight = 2.0F, stock = 3),
            AvailableCargoDTO(id = rum.id, name = "Rum", weight = 5.5F, stock = 2),
        )
    }

    @Test
    fun `Stock overview lists the whole catalog in catalog order, missing and zero entries as 0`() {
        val ale = aCargo(name = "Ale", weight = 2.0F)
        val rum = aCargo(name = "Rum", weight = 5.5F)
        val silk = aCargo(name = "Silk", weight = 2.0F)
        every { cargoQueryPort.findAllCargo() } returns listOf(ale, rum, silk)
        every { stockRepositoryPort.getStock() } returns mapOf(rum.id to 2, silk.id to 0)

        service.getStockOverview() shouldBe listOf(
            StockedCargoDTO(id = ale.id, name = "Ale", quantity = 0),
            StockedCargoDTO(id = rum.id, name = "Rum", quantity = 2),
            StockedCargoDTO(id = silk.id, name = "Silk", quantity = 0),
        )
    }
}
