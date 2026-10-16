package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.model.Cargo
import com.sonicdevelopment.domain.model.values.CargoId
import com.sonicdevelopment.domain.model.values.Money

/**
 * The Delivery Price rule, shared by the Incoming Ships list and the unloading, so that the price listed and
 * the price paid can never differ.
 */
object DeliveryPrice {
    /** The sum of the [prices] of every Cargo in [cargo], each counted as often as it is there; `null` if one has none. */
    fun of(cargo: List<Cargo>, prices: Map<CargoId, Money>): Money? {
        var sum = Money.dollars(0)
        for (each in cargo) sum += prices[each.id] ?: return null
        return sum
    }
}
