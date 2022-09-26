package at.willhaben.driven.adapter.persistence.ship

import at.willhaben.domain.model.Cargo
import at.willhaben.domain.model.Ship
import at.willhaben.domain.ports.driven.ShipQueryPort
import at.willhaben.driven.adapter.persistence.cargo.CargoPersistenceEntity
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class ShipQueryAdapter(
    private val shipRepository: ShipRepository
) : ShipQueryPort {

    override fun getAllShips(): List<Ship> {
        return shipRepository.findAll().map { toShip(it) }
    }

    override fun getShipDetails(shipId: Long): Ship? {
        return shipRepository.findByIdOrNull(shipId)?.let {
            toShip(it)
        }
    }

    private fun toShip(shipPersistenceEntity: ShipPersistenceEntity) =
        Ship(
            id = shipPersistenceEntity.id,
            name = shipPersistenceEntity.shipName,
            cargoLoad = shipPersistenceEntity.cargoLoad.map {
                toCargo(it)
            }.toMutableList()
        )

    private fun toCargo(cargoPersistenceEntity: CargoPersistenceEntity) =
        Cargo(
            id = cargoPersistenceEntity.id,
            name = cargoPersistenceEntity.cargoName,
            weight = cargoPersistenceEntity.cargoWeight
        )
}

