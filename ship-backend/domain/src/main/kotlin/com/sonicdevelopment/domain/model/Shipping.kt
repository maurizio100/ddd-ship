package com.sonicdevelopment.domain.model

import com.sonicdevelopment.domain.model.enums.ShippingState
import com.sonicdevelopment.domain.model.values.ShippingId

class Shipping(
    val id: ShippingId,
    val sailorsQuote: String?,
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
}
