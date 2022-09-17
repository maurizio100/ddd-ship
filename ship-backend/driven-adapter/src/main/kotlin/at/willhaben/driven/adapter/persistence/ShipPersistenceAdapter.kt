package at.willhaben.driven.adapter.persistence

import at.willhaben.domain.model.Ship
import at.willhaben.domain.ports.driven.ShipPersistencePort
import org.springframework.stereotype.Component

@Component
class ShipPersistenceAdapter(): ShipPersistencePort {
    override fun save(ship: Ship): Ship {
        TODO("Not yet implemented")
    }
}
