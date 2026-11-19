package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.exception.SavingsDoNotCoverException
import com.sonicdevelopment.domain.model.enums.ShippingState
import com.sonicdevelopment.domain.model.values.EventId
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.ports.driven.CargoPersistencePort
import com.sonicdevelopment.domain.ports.driven.CargoPersistencePort.CargoLoadInformation
import com.sonicdevelopment.domain.ports.driven.FleetEventsPort
import com.sonicdevelopment.domain.ports.driven.InboxRepositoryPort
import com.sonicdevelopment.domain.ports.driven.QuoteRepositoryPort
import com.sonicdevelopment.domain.ports.driven.SavingsRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShippingOutboxRepository
import com.sonicdevelopment.domain.ports.driven.ShippingRepositoryPort
import com.sonicdevelopment.domain.ports.driving.shipping.FinePaymentDTO
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
 *
 * At its Home Harbor, an Imprisoned Ship in port is freed by paying its [Fine] from the Savings ([payFine]), in one
 * transaction: the ship is read under its row lock, must be imprisoned here, the Fine is paid by the conditional
 * debit of the Savings, and only then is the imprisoned flag cleared. Nothing is written before the payment is
 * accepted. The lock serialises two payments for the same ship: the second finds it free and pays nothing.
 * Freeing tells no fleet and writes no outbox row.
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
    private val savingsRepositoryPort: SavingsRepositoryPort,
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

    @Transactional
    override fun payFine(shipId: ShipId): FinePaymentDTO? {
        val ship = shipRepositoryPort.lockShip(shipId) ?: return null
        ship.free(currentHarbor)
        if (!savingsRepositoryPort.pay(Fine.AMOUNT)) {
            throw SavingsDoNotCoverException(Fine.AMOUNT, "The Savings do not cover the Fine of ${Fine.AMOUNT}")
        }
        shipRepositoryPort.free(ship.id)
        return FinePaymentDTO(ship.id, ship.shipName, Fine.AMOUNT, savingsRepositoryPort.getSavings())
    }
}
