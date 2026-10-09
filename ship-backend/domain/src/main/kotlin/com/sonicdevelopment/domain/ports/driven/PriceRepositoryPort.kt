package com.sonicdevelopment.domain.ports.driven

import com.sonicdevelopment.domain.model.values.CargoId
import com.sonicdevelopment.domain.model.values.Money

/**
 * The Price of each Cargo at this Harbor. The write runs in the caller's transaction.
 */
interface PriceRepositoryPort {
    /** The Price of every Cargo the Harbor has rolled one for. */
    fun getPrices(): Map<CargoId, Money>

    /** Keeps the first Price of [cargoId]; returns `false`, and changes nothing, if it already has one. */
    fun rememberPrice(cargoId: CargoId, price: Money): Boolean
}
