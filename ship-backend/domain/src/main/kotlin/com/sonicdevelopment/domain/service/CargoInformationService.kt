package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.ports.driven.CargoQueryPort
import com.sonicdevelopment.domain.ports.driven.StockRepositoryPort
import com.sonicdevelopment.domain.ports.driving.cargo.AvailableCargoDTO
import com.sonicdevelopment.domain.ports.driving.cargo.CargoInformationPort
import com.sonicdevelopment.domain.ports.driving.cargo.StockedCargoDTO
import org.springframework.stereotype.Service

@Service
class CargoInformationService(
    private val cargoQueryPort: CargoQueryPort,
    private val stockRepositoryPort: StockRepositoryPort
): CargoInformationPort {

    /** The Available Cargo: the catalog Cargo this Harbor's Stock holds at least one of, in catalog order. */
    override fun getAvailableCargo(): List<AvailableCargoDTO> {
        val stock = stockRepositoryPort.getStock()
        return cargoQueryPort.findAllCargo().mapNotNull { cargo ->
            val quantity = stock[cargo.id] ?: 0
            if (quantity > 0) {
                AvailableCargoDTO(id = cargo.id, name = cargo.name, weight = cargo.weight, stock = quantity)
            } else {
                null
            }
        }
    }

    /** The Stock overview: every catalog Cargo, in catalog order, with its Stock, Cargo at 0 included. */
    override fun getStockOverview(): List<StockedCargoDTO> {
        val stock = stockRepositoryPort.getStock()
        return cargoQueryPort.findAllCargo().map { cargo ->
            StockedCargoDTO(id = cargo.id, name = cargo.name, quantity = stock[cargo.id] ?: 0)
        }
    }
}
