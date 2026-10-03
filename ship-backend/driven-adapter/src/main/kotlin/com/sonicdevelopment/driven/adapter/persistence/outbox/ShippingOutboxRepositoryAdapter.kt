package com.sonicdevelopment.driven.adapter.persistence.outbox

import com.fasterxml.jackson.databind.ObjectMapper
import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.ShippingId
import com.sonicdevelopment.domain.ports.driven.ShippingOutboxRepository
import com.sonicdevelopment.driven.adapter.persistence.outbox.events.ShippingEventConverter
import org.springframework.stereotype.Component

@Component
class ShippingOutboxRepositoryAdapter(
    val shippingOutboxPersistenceRepository: ShippingOutboxPersistenceRepository
) : ShippingOutboxRepository {

    /** A plain mapper on purpose: the published JSON must not change with the app's Jackson settings. */
    private val objectMapper = ObjectMapper()

    override fun broadcastShipping(ship: Ship, originHarbor: HarborName) {
        val shipping = ship.activeShipping ?: throw IllegalStateException()
        val shippingEvent = ShippingEventConverter.toShippingEvent(ship, originHarbor)

        val shippingOutboxPersistenceEntity = ShippingOutboxPersistenceEntity(
            aggregatetype = "shipping",
            aggregateId = shipping.id.id,
            type = "shipping-published",
            payload = objectMapper.writeValueAsString(shippingEvent)
        )

        shippingOutboxPersistenceRepository.save(
            shippingOutboxPersistenceEntity
        )
    }

    override fun announceShipArrived(
        ship: Ship,
        shippingId: ShippingId,
        originHarbor: HarborName,
        destinationHarbor: HarborName
    ) {
    }
}
