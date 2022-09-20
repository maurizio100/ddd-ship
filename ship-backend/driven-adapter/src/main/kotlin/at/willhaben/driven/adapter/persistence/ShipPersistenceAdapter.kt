package at.willhaben.driven.adapter.persistence

import at.willhaben.domain.model.Ship
import at.willhaben.domain.ports.driven.ShipPersistencePort
import org.springframework.stereotype.Component

@Component
class ShipPersistenceAdapter(
    private val shipRepository: ShipRepository
): ShipPersistencePort {
    override fun save(ship: Ship): Ship {
        val shipToPersist = ShipPersistenceEntity(
            shipName = ship.shipName
        )
        shipRepository.save(shipToPersist)
        return ship.apply {
            id = shipToPersist.id
        }
    }

    override fun delete(shipId: Long) {
        shipRepository.deleteById(shipId)
    }
}
