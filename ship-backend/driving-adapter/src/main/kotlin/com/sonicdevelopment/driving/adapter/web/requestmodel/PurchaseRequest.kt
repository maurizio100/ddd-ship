package com.sonicdevelopment.driving.adapter.web.requestmodel

import java.math.BigDecimal
import java.util.*

/**
 * A purchase at the Market. [quantity] is a `BigDecimal` so that `1.5` is refused, not truncated to 1
 * by Jackson's float-to-int coercion.
 */
data class PurchaseRequest(
    val cargoId: UUID? = null,
    val quantity: BigDecimal? = null
)
