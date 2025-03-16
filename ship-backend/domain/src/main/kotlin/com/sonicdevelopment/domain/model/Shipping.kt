package com.sonicdevelopment.domain.model

import com.sonicdevelopment.domain.model.enums.ShippingState
import com.sonicdevelopment.domain.model.values.ShippingId
import com.sonicdevelopment.domain.model.values.ShippingQuote

class Shipping(
    val id: ShippingId,
    var shippingQuote: ShippingQuote? = null,
    var shippingState: ShippingState = ShippingState.PREPARING
) {

    fun release(shippingQuote: ShippingQuote) {
        this.shippingState = ShippingState.SHIPPING
        this.shippingQuote = shippingQuote
    }
}
