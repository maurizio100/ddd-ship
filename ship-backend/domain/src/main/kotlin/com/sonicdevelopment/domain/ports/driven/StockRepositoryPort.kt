package com.sonicdevelopment.domain.ports.driven

import com.sonicdevelopment.domain.model.values.CargoId

/**
 * The Stock of this Harbor: how many of each Cargo it has on hand. One Stock per Harbor.
 *
 * The Stock never drops below 0. The write methods must run inside the caller's transaction, so a
 * Stock change commits or rolls back together with the cargo load that caused it.
 */
interface StockRepositoryPort {
    /**
     * Takes one [cargoId] out of the Stock in a single atomic step. Returns `false`, and leaves the
     * Stock unchanged, when the Stock holds none of it.
     */
    fun takeOneFromStock(cargoId: CargoId): Boolean

    /** Puts [quantity] of [cargoId] into the Stock, starting a Stock entry for it if it has none. */
    fun putIntoStock(cargoId: CargoId, quantity: Int = 1)

    /** The quantity on hand of every Cargo the Stock has an entry for. */
    fun getStock(): Map<CargoId, Int>
}
