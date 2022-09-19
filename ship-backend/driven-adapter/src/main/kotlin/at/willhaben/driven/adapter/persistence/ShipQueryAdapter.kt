package at.willhaben.driven.adapter.persistence

import at.willhaben.domain.model.Ship
import at.willhaben.domain.ports.driven.ShipQueryPort
import org.springframework.stereotype.Component

@Component
class ShipQueryAdapter(
    private val shipRepository: ShipRepository
) : ShipQueryPort {

    override fun getAllShips(): List<Ship> {
        return shipRepository.findAll().map { toShip(it) }
    }

    private fun toShip(shipPersistenceEntity: ShipPersistenceEntity) =
        Ship(
            id = shipPersistenceEntity.id,
            name = shipPersistenceEntity.shipName
        )
}
