package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.model.values.CargoId
import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.domain.ports.driven.CargoQueryPort
import com.sonicdevelopment.domain.ports.driven.PriceRepositoryPort
import com.sonicdevelopment.domain.ports.driven.SavingsRepositoryPort
import com.sonicdevelopment.domain.ports.driven.StockRepositoryPort
import com.sonicdevelopment.domain.ports.driving.market.MarketPort
import org.springframework.stereotype.Service

@Service
class MarketService(
    private val cargoQueryPort: CargoQueryPort,
    private val priceRepositoryPort: PriceRepositoryPort,
    private val savingsRepositoryPort: SavingsRepositoryPort,
    private val stockRepositoryPort: StockRepositoryPort
) : MarketPort {

    override fun buyCargo(cargoId: CargoId, quantity: Int): Money? = TODO("STORY-025")
}
