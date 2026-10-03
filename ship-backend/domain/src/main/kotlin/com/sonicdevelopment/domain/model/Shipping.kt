package com.sonicdevelopment.domain.model

import com.sonicdevelopment.domain.model.enums.ShippingState
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.ShippingId
import com.sonicdevelopment.domain.model.values.ShippingQuote

class Shipping(
    val id: ShippingId,
    var shippingQuote: ShippingQuote? = null,
    var shippingState: ShippingState = ShippingState.PREPARING,
    var destinationHarbor: HarborName? = null
) {

    fun release(shippingQuote: ShippingQuote, destinationHarbor: HarborName) {
        this.shippingState = ShippingState.SHIPPING
        this.shippingQuote = shippingQuote
        this.destinationHarbor = destinationHarbor
    }

    /** The voyage is over. Only [Ship.endShipping] calls this, and only for a Shipping at sea. */
    fun end() {
        this.shippingState = ShippingState.DONE
    }
}
