package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.exception.CargoHasNoPriceException
import com.sonicdevelopment.domain.exception.SavingsDoNotCoverException
import com.sonicdevelopment.domain.exception.ShipNotIncomingException
import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.ports.driven.CargoPersistencePort
import com.sonicdevelopment.domain.ports.driven.CargoPersistencePort.CargoLoadInformation
import com.sonicdevelopment.domain.ports.driven.PriceRepositoryPort
import com.sonicdevelopment.domain.ports.driven.QuoteRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShippingOutboxRepository
import com.sonicdevelopment.domain.ports.driven.ShippingRepositoryPort
import com.sonicdevelopment.domain.ports.driven.SavingsRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort
import com.sonicdevelopment.domain.ports.driven.StockRepositoryPort
import com.sonicdevelopment.domain.ports.driving.harbor.IncomingShipManagementPort
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

/**
 * Unloads an Incoming Ship in one transaction, in this order: it **pays** the Delivery Price first, so an
 * unloading the Savings cannot cover is refused before anything is written; then it puts every Cargo aboard
 * into the Stock; and it **clears** the ship last, with a conditional step that is re-checked under the ship's
 * row lock. Of two concurrent unloadings of the same ship only one clears it; the other fails there, after
 * paying, and its transaction rolls its payment and its Stock writes back.
 *
 * Refuses an Incoming Ship in one transaction, in the order of a Release: it **clears** the ship first, with the
 * same conditional step, then writes the new Shipping, its Loaded Cargo, the Release to the Home Harbor, and the
 * Shipping Published. The conditional step comes first so that, of a refusal and an unloading of the same ship,
 * only the one that clears it takes effect: a refusal that loses writes nothing, and an unloading that loses
 * rolls back. Nothing is paid and the Stock is not touched.
 */
@Service
class IncomingShipManagementService(
    private val shipRepositoryPort: ShipRepositoryPort,
    private val priceRepositoryPort: PriceRepositoryPort,
    private val savingsRepositoryPort: SavingsRepositoryPort,
    private val stockRepositoryPort: StockRepositoryPort,
    private val shippingRepositoryPort: ShippingRepositoryPort,
    private val cargoPersistencePort: CargoPersistencePort,
    private val quoteRepositoryPort: QuoteRepositoryPort,
    private val shippingOutboxRepository: ShippingOutboxRepository,
    private val currentHarbor: HarborName,
) : IncomingShipManagementPort {

    @Transactional
    override fun unloadIncomingShip(shipId: ShipId): Money? {
        val ship = shipRepositoryPort.getShipDetails(shipId) ?: return null
        val cargo = ship.unload()

        val prices = priceRepositoryPort.getPrices()
        val deliveryPrice = DeliveryPrice.of(cargo, prices) ?: throw CargoHasNoPriceException(
            "${cargo.first { it.id !in prices }.name} has no Price yet, so ${ship.shipName} cannot be unloaded"
        )

        if (!savingsRepositoryPort.pay(deliveryPrice)) {
            throw SavingsDoNotCoverException(deliveryPrice, "The Savings do not cover the Delivery Price of $deliveryPrice")
        }
        cargo.forEach { stockRepositoryPort.putIntoStock(it.id, 1) }

        if (!shipRepositoryPort.unloadIncomingShip(ship.id)) {
            throw ShipNotIncomingException("${ship.shipName} is not an Incoming Ship anymore")
        }
        return deliveryPrice
    }

    @Transactional
    override fun refuseIncomingShip(shipId: ShipId): HarborName? {
        val ship = shipRepositoryPort.getShipDetails(shipId) ?: return null
        ship.refuse(currentHarbor)

        if (!shipRepositoryPort.unloadIncomingShip(ship.id)) {
            throw ShipNotIncomingException("${ship.shipName} is not an Incoming Ship anymore")
        }
        shippingRepositoryPort.createShipping(ship)
        cargoPersistencePort.updateCargoLoad(CargoLoadInformation.fromShip(ship))
        ship.release(quoteRepositoryPort.getQuoteForSailorsCode(ship.createSailorsCode()), ship.homeHarbor)
        shippingRepositoryPort.updateActiveShipping(ship)
        shippingOutboxRepository.broadcastShipping(ship, currentHarbor)
        return ship.homeHarbor
    }
}
