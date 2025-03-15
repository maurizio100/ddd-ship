package com.sonicdevelopment.domain.model

import com.sonicdevelopment.domain.model.enums.ShippingState
import com.sonicdevelopment.domain.model.values.ShippingId
import com.sonicdevelopment.domain.model.values.ShippingQuote

class Shipping(
    val id: ShippingId,
    private var _shippingQuote: ShippingQuote? = null,
    private var _shippingState: ShippingState = ShippingState.PREPARING
) {

    var shippingState = _shippingState
        get() = field
    fun nextShippingState() {
        _shippingState = when(shippingState) {
            ShippingState.PREPARING -> ShippingState.SHIPPING
            ShippingState.SHIPPING -> ShippingState.DONE
            ShippingState.DONE -> ShippingState.DONE
        }
    }

    var shippingQuote = _shippingQuote
        get() = field
    fun release(shippingQuote: ShippingQuote) {
        _shippingState = ShippingState.SHIPPING
        _shippingQuote = shippingQuote
    }
}
