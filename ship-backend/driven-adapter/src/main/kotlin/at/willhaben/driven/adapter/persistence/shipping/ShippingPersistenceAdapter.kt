package at.willhaben.driven.adapter.persistence.shipping

import at.willhaben.domain.model.Ship
import at.willhaben.domain.ports.driven.ShippingPersistencePort
import at.willhaben.driven.adapter.persistence.ship.ShipRepository
import org.springframework.stereotype.Component

@Component
class ShippingPersistenceAdapter(
    private val shippingRepository: ShippingRepository,
    private val shipRepository: ShipRepository
): ShippingPersistencePort {
    override fun createShipping(ship: Ship): Ship {
        val shipping = ShippingPersistenceEntity(
            sailorsCode = ship.shipping?.sailorsQuote!!,
            ship = shipRepository.getReferenceById(ship.id!!)
        )
        val persistedShipping = shippingRepository.save(shipping)
        ship.shipping?.id = persistedShipping.id
        return ship
    }
}
