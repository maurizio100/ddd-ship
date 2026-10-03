package com.sonicdevelopment.driven.adapter.persistence.arrival

import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.model.values.ShippingId
import com.sonicdevelopment.domain.ports.driven.ArrivalRepositoryPort
import org.springframework.stereotype.Component

@Component
class ArrivalRepositoryAdapter : ArrivalRepositoryPort {

    override fun recordArrival(shippingId: ShippingId, shipId: ShipId): Boolean =
        TODO("STORY-007 review: record the Arrival")
}
