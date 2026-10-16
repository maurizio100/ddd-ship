package com.sonicdevelopment.domain.ports.driving.market

import com.sonicdevelopment.domain.model.values.CargoId
import com.sonicdevelopment.domain.model.values.Money

/** The Market, where the User buys Cargo for this Harbor's Stock with its Savings. */
interface MarketPort {
    /**
     * Buys [quantity] of [cargoId] at its Price: pays Price × [quantity] from the Savings and puts the
     * Cargo into the Stock, in one transaction. Returns the cost paid, or `null` when the Cargo is unknown.
     */
    fun buyCargo(cargoId: CargoId, quantity: Int): Money?
}
