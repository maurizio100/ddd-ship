package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.enums.ShippingState
import com.sonicdevelopment.domain.model.values.EventId
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.ports.driven.ArrivalRepositoryPort
import com.sonicdevelopment.domain.ports.driven.CargoQueryPort
import com.sonicdevelopment.domain.ports.driven.CatainRepository
import com.sonicdevelopment.domain.ports.driven.FleetEventsPort
import com.sonicdevelopment.domain.ports.driven.InboxRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort.InitialShipInformation.Companion.fromShip
import com.sonicdevelopment.domain.ports.driven.ShippingOutboxRepository
import com.sonicdevelopment.domain.ports.driven.ShippingRepositoryPort
import com.sonicdevelopment.domain.ports.driving.shipping.ArrivalManagementPort
import com.sonicdevelopment.domain.ports.driving.shipping.ShipArrivedDTO
import com.sonicdevelopment.domain.ports.driving.shipping.ShippingPublishedDTO
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

/**
 * Both sides of the Arrival.
 *
 * **Destination side.** A ship Released to this Harbor arrives by itself when its Shipping Published is consumed.
 *
 * Two guards make it take effect at most once per Release. The inbox drops a redelivered event (same
 * EventId). A re-publication of the same Release arrives under a new EventId, so the Origin Harbor's
 * Shipping id is the business key: each handled Arrival is recorded by it, and an Arrival whose Shipping
 * has already arrived is skipped, even after the ship has sailed on and left this Harbor's fleet.
 * Events addressed to another Harbor, or without a Destination Harbor, are recorded and ignored.
 *
 * A ship already in the fleet arrives only if it is still at sea from here: it came back before this
 * Harbor learned of its earlier Arrival elsewhere (arc42 R-10). That earlier voyage ends implicitly
 * (`DONE`) and the ship stays in the fleet, so the late Ship Arrived for it finds nothing at sea and has
 * no effect. A ship in the fleet that is not at sea does not arrive again: an Arrival handled before the
 * Arrivals were recorded, re-published.
 *
 * Otherwise, in one transaction with the inbox record: the ship joins the fleet with its Ship Id, Ship Name
 * and Catain and no Shipping, remembering the Origin Harbor it arrived from (replacing the one of an earlier
 * Arrival), and Ship Arrived is written to the outbox for the Origin Harbor. A ship that carries Cargo joins
 * as an Incoming Ship with that Cargo aboard, one entry per Cargo instance; a ship that carries none joins as
 * an ordinary ship. The Stock is unchanged: Cargo goes into it only when a User unloads the ship (STORY-045).
 * The ship keeps the Home Harbor carried in the Shipping Published; an event without one (from a Harbor that
 * does not send it yet) makes the Origin Harbor the ship's Home Harbor.
 *
 * **Origin side.** When this Harbor learns from Ship Arrived that a ship it Released has arrived, the
 * voyage ends: in one transaction with the inbox record, the Shipping becomes `DONE` and the ship leaves
 * this Harbor's fleet (its record and Shippings are kept). Events for another Origin Harbor, or for a ship
 * not in the fleet, are recorded and ignored. The Shipping id guards against a late Ship Arrived: only the
 * ship's Active Shipping with that id, still at sea, ends, so a later voyage is never ended by it.
 *
 * Both sides tell the Users at this Harbor that the fleet changed (ship arrived, ship left) through
 * [FleetEventsPort], which takes effect only once the transaction commits; an ignored event tells nothing.
 */
@Service
class ArrivalManagementService(
    private val currentHarbor: HarborName,
    private val inboxRepositoryPort: InboxRepositoryPort,
    private val shipRepositoryPort: ShipRepositoryPort,
    private val catainRepository: CatainRepository,
    private val cargoQueryPort: CargoQueryPort,
    private val shippingOutboxRepository: ShippingOutboxRepository,
    private val shippingRepositoryPort: ShippingRepositoryPort,
    private val arrivalRepositoryPort: ArrivalRepositoryPort,
    private val fleetEventsPort: FleetEventsPort,
) : ArrivalManagementPort {

    @Transactional
    override fun receiveShippingPublished(eventId: EventId, shippingPublished: ShippingPublishedDTO) {
        if (!inboxRepositoryPort.recordConsumedEvent(eventId)) return
        if (shippingPublished.destinationHarbor != currentHarbor) return
        if (!arrivalRepositoryPort.recordArrival(shippingPublished.shippingId, shippingPublished.shipId)) return
        val inFleet = shipRepositoryPort.getShipDetails(shippingPublished.shipId)
        val earlierVoyage = inFleet?.activeShipping?.takeIf { it.shippingState == ShippingState.SHIPPING }
        if (inFleet != null && earlierVoyage == null) return

        val catain = catainRepository.findCatainById(shippingPublished.catainId)
            ?: throw IllegalStateException("Unknown Catain ${shippingPublished.catainId.id} on arriving ship ${shippingPublished.shipId.id}")
        val cargo = shippingPublished.cargoIds.map {
            cargoQueryPort.findCargo(it)
                ?: throw IllegalStateException("Unknown Cargo ${it.id} on arriving ship ${shippingPublished.shipId.id}")
        }
        val originHarbor = shippingPublished.originHarbor
            ?: throw IllegalStateException("Arriving ship ${shippingPublished.shipId.id} has no Origin Harbor")

        if (inFleet != null && earlierVoyage != null) {
            inFleet.endShipping(earlierVoyage.id)
            shippingRepositoryPort.updateActiveShipping(inFleet)
        }
        val ship = Ship(
            id = shippingPublished.shipId,
            name = shippingPublished.shipName,
            catainId = catain.catainId,
            catainName = catain.catainName,
            homeHarbor = shippingPublished.homeHarbor ?: originHarbor,
            arrivedFrom = originHarbor,
            cargoAboard = cargo,
            incoming = cargo.isNotEmpty(),
        )
        shipRepositoryPort.saveNewShip(fromShip(ship))
        shippingOutboxRepository.announceShipArrived(ship, shippingPublished.shippingId, originHarbor, currentHarbor)
        fleetEventsPort.announceShipArrived(ship, originHarbor)
    }

    @Transactional
    override fun receiveShipArrived(eventId: EventId, shipArrived: ShipArrivedDTO) {
        if (!inboxRepositoryPort.recordConsumedEvent(eventId)) return
        if (shipArrived.originHarbor != currentHarbor) return
        val ship = shipRepositoryPort.getShipDetails(shipArrived.shipId) ?: return
        if (!ship.endShipping(shipArrived.shippingId)) return

        shippingRepositoryPort.updateActiveShipping(ship)
        shipRepositoryPort.removeFromFleet(ship.id)
        fleetEventsPort.announceShipLeft(ship, shipArrived.destinationHarbor)
    }
}
