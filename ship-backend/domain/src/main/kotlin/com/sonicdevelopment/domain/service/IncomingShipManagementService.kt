package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.exception.CargoHasNoPriceException
import com.sonicdevelopment.domain.exception.SavingsDoNotCoverException
import com.sonicdevelopment.domain.exception.ShipNotIncomingException
import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.ports.driven.PriceRepositoryPort
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
 */
@Service
class IncomingShipManagementService(
    private val shipRepositoryPort: ShipRepositoryPort,
    private val priceRepositoryPort: PriceRepositoryPort,
    private val savingsRepositoryPort: SavingsRepositoryPort,
    private val stockRepositoryPort: StockRepositoryPort,
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
}
