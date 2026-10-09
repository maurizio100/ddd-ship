package com.sonicdevelopment.application.acceptance.fixtures

import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.ShippingId
import com.sonicdevelopment.domain.ports.driven.HarborOutboxRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShippingOutboxRepository
import java.util.UUID

/** What was written to the outbox, as domain-level values rather than JSON. */
sealed interface OutboxMessage {
    data class ShippingPublished(
        val shipId: UUID,
        val shipName: String,
        val shippingId: UUID,
        val cargoIds: List<UUID>,
        val originHarbor: String,
        val destinationHarbor: String?,
    ) : OutboxMessage

    data class ShipArrived(
        val shipId: UUID,
        val shipName: String,
        val shippingId: UUID,
        val originHarbor: String,
        val destinationHarbor: String,
    ) : OutboxMessage

    data class HarborOpened(val harborName: String) : OutboxMessage
}

class InMemoryOutbox : ShippingOutboxRepository, HarborOutboxRepositoryPort {

    private val messages = mutableListOf<OutboxMessage>()

    @Synchronized
    override fun broadcastShipping(ship: Ship, originHarbor: HarborName) {
        val shipping = ship.activeShipping ?: throw IllegalStateException()
        messages += OutboxMessage.ShippingPublished(
            shipId = ship.id.id,
            shipName = ship.shipName,
            shippingId = shipping.id.id,
            cargoIds = ship.loadedCargo.map { it.id.id },
            originHarbor = originHarbor.name,
            destinationHarbor = shipping.destinationHarbor?.name,
        )
    }

    @Synchronized
    override fun announceShipArrived(
        ship: Ship,
        shippingId: ShippingId,
        originHarbor: HarborName,
        destinationHarbor: HarborName,
    ) {
        messages += OutboxMessage.ShipArrived(
            shipId = ship.id.id,
            shipName = ship.shipName,
            shippingId = shippingId.id,
            originHarbor = originHarbor.name,
            destinationHarbor = destinationHarbor.name,
        )
    }

    @Synchronized
    override fun publishHarborOpened(harborName: HarborName) {
        messages += OutboxMessage.HarborOpened(harborName.name)
    }

    @Synchronized
    fun messages(): List<OutboxMessage> = messages.toList()

    fun shippingPublished(): List<OutboxMessage.ShippingPublished> =
        messages().filterIsInstance<OutboxMessage.ShippingPublished>()

    fun shipArrived(): List<OutboxMessage.ShipArrived> =
        messages().filterIsInstance<OutboxMessage.ShipArrived>()

    @Synchronized
    fun reset() {
        messages.clear()
    }
}
