package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.exception.CargoHasNoPriceException
import com.sonicdevelopment.domain.exception.SavingsDoNotCoverException
import com.sonicdevelopment.domain.model.values.CargoId
import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.domain.ports.driven.CargoQueryPort
import com.sonicdevelopment.domain.ports.driven.PriceRepositoryPort
import com.sonicdevelopment.domain.ports.driven.SavingsRepositoryPort
import com.sonicdevelopment.domain.ports.driven.StockRepositoryPort
import com.sonicdevelopment.domain.ports.driving.market.MarketPort
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class MarketService(
    private val cargoQueryPort: CargoQueryPort,
    private val priceRepositoryPort: PriceRepositoryPort,
    private val savingsRepositoryPort: SavingsRepositoryPort,
    private val stockRepositoryPort: StockRepositoryPort
) : MarketPort {

    /**
     * Pays before it stocks: a purchase the Savings cannot cover is refused before anything is written. A
     * Stock write that fails after the payment rolls the payment back with this transaction.
     */
    @Transactional
    override fun buyCargo(cargoId: CargoId, quantity: Int): Money? {
        require(quantity > 0) { "A purchase needs a quantity of at least 1, not $quantity" }
        val cargo = cargoQueryPort.findCargo(cargoId) ?: return null
        val price = priceRepositoryPort.getPrices()[cargo.id]
            ?: throw CargoHasNoPriceException("${cargo.name} has no Price yet")

        val cost = price * quantity
        if (!savingsRepositoryPort.pay(cost)) {
            throw SavingsDoNotCoverException(cost)
        }
        stockRepositoryPort.putIntoStock(cargo.id, quantity)

        return cost
    }
}
