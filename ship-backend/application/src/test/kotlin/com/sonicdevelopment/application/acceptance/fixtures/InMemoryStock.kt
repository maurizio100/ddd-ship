package com.sonicdevelopment.application.acceptance.fixtures

import com.sonicdevelopment.domain.model.values.CargoId
import com.sonicdevelopment.domain.ports.driven.StockRepositoryPort
import java.util.UUID

/** The Stock in memory. Mirrors the adapter's native queries: `putIntoStock` adds to an existing entry. */
class InMemoryStock(private val catalog: InMemoryCargoCatalog) : StockRepositoryPort {

    private val quantities = LinkedHashMap<CargoId, Int>()

    init {
        reset()
    }

    @Synchronized
    override fun takeOneFromStock(cargoId: CargoId): Boolean {
        val quantity = quantities[cargoId] ?: return false
        if (quantity <= 0) return false
        quantities[cargoId] = quantity - 1
        return true
    }

    @Synchronized
    override fun putIntoStock(cargoId: CargoId, quantity: Int) {
        quantities[cargoId] = (quantities[cargoId] ?: 0) + quantity
    }

    @Synchronized
    override fun getStock(): Map<CargoId, Int> = LinkedHashMap(quantities)

    /** Puts the Stock back to the Starting Stock: [STARTING_STOCK] of every catalog Cargo. */
    @Synchronized
    fun reset() {
        quantities.clear()
        catalog.findAllCargo().forEach { quantities[it.id] = STARTING_STOCK }
    }

    @Synchronized
    fun setQuantity(cargoId: UUID, quantity: Int) {
        val id = CargoId(cargoId)
        check(id in quantities) { "No Stock entry for $cargoId" }
        quantities[id] = quantity
    }
}
