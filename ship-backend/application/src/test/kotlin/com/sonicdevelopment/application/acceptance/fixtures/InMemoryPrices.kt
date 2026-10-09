package com.sonicdevelopment.application.acceptance.fixtures

import com.sonicdevelopment.domain.model.values.CargoId
import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.domain.ports.driven.PriceRepositoryPort

/** The Prices in memory. Mirrors the adapter: the first write wins. A fresh Harbor has none. */
class InMemoryPrices : PriceRepositoryPort {

    private val prices = LinkedHashMap<CargoId, String>()

    @Synchronized
    override fun getPrices(): Map<CargoId, Money> = prices.mapValuesTo(LinkedHashMap()) { Money(java.math.BigDecimal(it.value)) }

    @Synchronized
    override fun rememberPrice(cargoId: CargoId, price: Money): Boolean {
        if (cargoId in prices) return false
        prices[cargoId] = price.amount.toPlainString()
        return true
    }

    /** Sets the Price of [cargoId] to [decimal], replacing any other. */
    @Synchronized
    fun setPrice(cargoId: CargoId, decimal: String) {
        prices[cargoId] = decimal
    }

    @Synchronized
    fun reset() {
        prices.clear()
    }
}
