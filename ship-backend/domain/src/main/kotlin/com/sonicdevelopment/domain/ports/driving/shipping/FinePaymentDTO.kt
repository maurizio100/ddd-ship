package com.sonicdevelopment.domain.ports.driving.shipping

import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.domain.model.values.ShipId

/** A paid Fine: the ship it freed, the [fine] taken from the Savings, and the [savings] left after it. */
data class FinePaymentDTO(
    val shipId: ShipId,
    val shipName: String,
    val fine: Money,
    val savings: Money,
)
