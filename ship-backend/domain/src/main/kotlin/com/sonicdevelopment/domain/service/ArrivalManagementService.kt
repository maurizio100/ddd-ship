package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.values.EventId
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.ports.driven.CargoQueryPort
import com.sonicdevelopment.domain.ports.driven.CatainRepository
import com.sonicdevelopment.domain.ports.driven.InboxRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort.InitialShipInformation.Companion.fromShip
import com.sonicdevelopment.domain.ports.driven.ShippingOutboxRepository
import com.sonicdevelopment.domain.ports.driven.StockRepositoryPort
import com.sonicdevelopment.domain.ports.driving.shipping.ArrivalManagementPort
import com.sonicdevelopment.domain.ports.driving.shipping.ShippingPublishedDTO
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

/**
 * The Arrival: a ship Released to this Harbor arrives by itself when its Shipping Published is consumed.
 *
 * Two guards make it take effect at most once per ship. The inbox drops a redelivered event (same
 * EventId). A re-publication of the same Release arrives under a new EventId, so the Ship Id is the
 * business key: a ship at sea is not in this Harbor's fleet unless it has already arrived here.
 * Events addressed to another Harbor, or without a Destination Harbor, are recorded and ignored.
 *
 * Otherwise, in one transaction with the inbox record: one of each Loaded Cargo goes into the Stock,
 * the ship joins the fleet with its Ship Id, Ship Name and Catain and no Shipping, and Ship Arrived is
 * written to the outbox for the Origin Harbor.
 */
@Service
class ArrivalManagementService(
    private val currentHarbor: HarborName,
    private val inboxRepositoryPort: InboxRepositoryPort,
    private val shipRepositoryPort: ShipRepositoryPort,
    private val catainRepository: CatainRepository,
    private val cargoQueryPort: CargoQueryPort,
    private val stockRepositoryPort: StockRepositoryPort,
    private val shippingOutboxRepository: ShippingOutboxRepository,
) : ArrivalManagementPort {

    @Transactional
    override fun receiveShippingPublished(eventId: EventId, shippingPublished: ShippingPublishedDTO) {
        if (!inboxRepositoryPort.recordConsumedEvent(eventId)) return
        if (shippingPublished.destinationHarbor != currentHarbor) return
        if (shipRepositoryPort.getShipDetails(shippingPublished.shipId) != null) return

        val catain = catainRepository.findCatainById(shippingPublished.catainId)
            ?: throw IllegalStateException("Unknown Catain ${shippingPublished.catainId.id} on arriving ship ${shippingPublished.shipId.id}")
        val cargo = shippingPublished.cargoIds.map {
            cargoQueryPort.findCargo(it)
                ?: throw IllegalStateException("Unknown Cargo ${it.id} on arriving ship ${shippingPublished.shipId.id}")
        }
        val originHarbor = shippingPublished.originHarbor
            ?: throw IllegalStateException("Arriving ship ${shippingPublished.shipId.id} has no Origin Harbor")

        cargo.forEach { stockRepositoryPort.putIntoStock(it.id, 1) }
        val ship = Ship(
            id = shippingPublished.shipId,
            name = shippingPublished.shipName,
            catainId = catain.catainId,
            catainName = catain.catainName,
        )
        shipRepositoryPort.saveNewShip(fromShip(ship))
        shippingOutboxRepository.announceShipArrived(ship, shippingPublished.shippingId, originHarbor, currentHarbor)
    }
}
