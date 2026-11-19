package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.model.enums.ShippingState
import com.sonicdevelopment.domain.model.values.EventId
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.ports.driven.CargoPersistencePort
import com.sonicdevelopment.domain.ports.driven.CargoPersistencePort.CargoLoadInformation
import com.sonicdevelopment.domain.ports.driven.FleetEventsPort
import com.sonicdevelopment.domain.ports.driven.InboxRepositoryPort
import com.sonicdevelopment.domain.ports.driven.QuoteRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShippingOutboxRepository
import com.sonicdevelopment.domain.ports.driven.ShippingRepositoryPort
import com.sonicdevelopment.domain.ports.driving.shipping.ImprisonmentManagementPort
import com.sonicdevelopment.domain.ports.driving.shipping.ShipImprisonedDTO
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

/**
 * Carries out the Coast Guard's imprisonment of a ship (Ship Imprisoned), in one transaction with the inbox record.
 *
 * Guards first, in this order: the inbox drops a redelivered event; only the Harbor named as holding the ship acts;
 * the ship is read under its row lock ([ShipRepositoryPort.lockShip]) and must be in this Harbor's fleet and not at
 * sea, or it escapes. Anything that escapes writes nothing but the inbox row. The lock serialises the confiscation
 * against a concurrent unloading or refusal of the same ship: whichever runs second finds it no longer Incoming and
 * rolls back.
 *
 * All the ship's Cargo is confiscated: into no Stock, and nothing is paid. At its Home Harbor the ship becomes an
 * Imprisoned Ship in place. Anywhere else it is Released to its Home Harbor without a User, reusing a `PREPARING`
 * Shipping or starting a new one, with no Cargo and flagged imprisoned in Shipping Published, as a refusal
 * sends a ship home. A second imprisonment of a ship already sent home finds it at sea and changes nothing.
 *
 * The Users at this Harbor are told through [FleetEventsPort] once the transaction commits.
 */
@Service
class ImprisonmentManagementService(
    private val currentHarbor: HarborName,
    private val inboxRepositoryPort: InboxRepositoryPort,
    private val shipRepositoryPort: ShipRepositoryPort,
    private val shippingRepositoryPort: ShippingRepositoryPort,
    private val cargoPersistencePort: CargoPersistencePort,
    private val quoteRepositoryPort: QuoteRepositoryPort,
    private val shippingOutboxRepository: ShippingOutboxRepository,
    private val fleetEventsPort: FleetEventsPort,
) : ImprisonmentManagementPort {

    @Transactional
    override fun receiveShipImprisoned(eventId: EventId, shipImprisoned: ShipImprisonedDTO) {
        if (!inboxRepositoryPort.recordConsumedEvent(eventId)) return
        if (shipImprisoned.holdingHarbor != currentHarbor) return
        val ship = shipRepositoryPort.lockShip(shipImprisoned.shipId) ?: return
        val wasPreparing = ship.shippingState() == ShippingState.PREPARING
        if (!ship.confiscate(currentHarbor)) return

        shipRepositoryPort.confiscate(ship.id)
        if (ship.homeHarbor == currentHarbor) {
            if (wasPreparing) cargoPersistencePort.updateCargoLoad(CargoLoadInformation.fromShip(ship))
            fleetEventsPort.announceShipImprisoned(ship, sailsHome = false)
            return
        }

        if (!wasPreparing) shippingRepositoryPort.createShipping(ship)
        cargoPersistencePort.updateCargoLoad(CargoLoadInformation.fromShip(ship))
        ship.sailHomeImprisoned(quoteRepositoryPort.getQuoteForSailorsCode(ship.createSailorsCode()))
        shippingRepositoryPort.updateActiveShipping(ship)
        shippingOutboxRepository.broadcastShipping(ship, currentHarbor)
        fleetEventsPort.announceShipImprisoned(ship, sailsHome = true)
    }
}
