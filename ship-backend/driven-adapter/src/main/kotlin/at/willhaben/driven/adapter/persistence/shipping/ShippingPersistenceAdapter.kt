package at.willhaben.driven.adapter.persistence.shipping

import at.willhaben.domain.model.Ship
import at.willhaben.domain.ports.driven.ShippingPersistencePort
import org.springframework.stereotype.Component

@Component
class ShippingPersistenceAdapter(): ShippingPersistencePort {
    override fun createShipping(ship: Ship): Ship {
        TODO("Not yet implemented")
    }
}
