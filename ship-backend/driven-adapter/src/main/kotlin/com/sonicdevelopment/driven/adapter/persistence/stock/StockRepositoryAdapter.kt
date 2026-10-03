package com.sonicdevelopment.driven.adapter.persistence.stock

import com.sonicdevelopment.domain.model.values.CargoId
import com.sonicdevelopment.domain.ports.driven.StockRepositoryPort
import org.springframework.stereotype.Component

@Component
class StockRepositoryAdapter : StockRepositoryPort {
    override fun takeOneFromStock(cargoId: CargoId): Boolean = TODO("STORY-004")

    override fun putIntoStock(cargoId: CargoId, quantity: Int): Unit = TODO("STORY-004")

    override fun getStock(): Map<CargoId, Int> = TODO("STORY-004")
}
