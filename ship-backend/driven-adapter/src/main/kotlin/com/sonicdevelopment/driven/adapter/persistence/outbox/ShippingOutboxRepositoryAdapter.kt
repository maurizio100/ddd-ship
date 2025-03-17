package com.sonicdevelopment.driven.adapter.persistence.outbox

import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.ports.driven.ShippingOutboxRepository
import com.sonicdevelopment.driven.adapter.persistence.outbox.events.ShippingEventConverter
import org.springframework.stereotype.Component

@Component
class ShippingOutboxRepositoryAdapter(
    val shippingOutboxPersistenceRepository: ShippingOutboxPersistenceRepository
) : ShippingOutboxRepository {

    override fun broadcastShipping(ship: Ship) {
        val shipping = ship.activeShipping ?: throw IllegalStateException()
        val shippingEvent = ShippingEventConverter.toShippingEvent(ship)

        val shippingOutboxPersistenceEntity = ShippingOutboxPersistenceEntity(
            aggregatetype = "shipping",
            aggregateId = shipping.id.id,
            type = "shipping-published",
            payload = shippingEvent
        )

        shippingOutboxPersistenceRepository.save(
            shippingOutboxPersistenceEntity
        )
    }

}