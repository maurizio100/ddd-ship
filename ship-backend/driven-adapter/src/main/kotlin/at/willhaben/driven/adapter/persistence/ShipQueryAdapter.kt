package at.willhaben.driven.adapter.persistence

import at.willhaben.domain.model.Ship
import at.willhaben.domain.ports.driven.ShipQueryPort
import org.springframework.stereotype.Component

@Component
class ShipQueryAdapter() : ShipQueryPort {
    override fun getAllShips(): List<Ship> {
        TODO("Not yet implemented")
    }
}
