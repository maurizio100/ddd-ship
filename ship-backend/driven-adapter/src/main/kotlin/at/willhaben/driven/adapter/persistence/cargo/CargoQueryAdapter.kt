package at.willhaben.driven.adapter.persistence.cargo

import at.willhaben.domain.model.Cargo
import at.willhaben.domain.ports.driven.CargoQueryPort
import org.springframework.stereotype.Component

@Component
class CargoQueryAdapter(): CargoQueryPort {
    override fun findAvailableCargo(): List<Cargo> {
        TODO("Not yet implemented")
    }
}
