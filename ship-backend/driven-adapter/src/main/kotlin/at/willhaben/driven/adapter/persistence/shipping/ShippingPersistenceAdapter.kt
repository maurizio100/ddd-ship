package at.willhaben.driven.adapter.persistence.shipping

import at.willhaben.domain.model.Ship
import at.willhaben.domain.ports.driven.ShippingPersistencePort
import at.willhaben.driven.adapter.persistence.ship.ShipRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class ShippingPersistenceAdapter(
    private val shippingRepository: ShippingRepository,
    private val shipRepository: ShipRepository
): ShippingPersistencePort {
    override fun createShipping(ship: Ship): Ship {
        val persistedShip = shipRepository.findByIdOrNull(ship.id) ?: return ship

        val shipping = ShippingPersistenceEntity(
            id = ship.shipping?.id,
            sailorsCode = ship.shipping?.sailorsQuote!!,
            ship = persistedShip
        )
        val persistedShipping = shippingRepository.save(shipping)
        persistedShip.shipping = persistedShipping

        shipRepository.save(persistedShip)
        ship.shipping?.id = persistedShipping.id
        return ship
    }
}
