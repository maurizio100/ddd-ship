package at.willhaben.domain.service

import at.willhaben.domain.model.Cargo
import at.willhaben.domain.ports.driven.CargoQueryPort
import at.willhaben.domain.ports.driving.cargo.CargoDTO
import at.willhaben.domain.ports.driving.cargo.CargoInformationPort
import org.springframework.stereotype.Service

@Service
class CargoInformationService(
    private val cargoQueryPort: CargoQueryPort
): CargoInformationPort {
    override fun getAvailableCargo(): List<CargoDTO> {
        return cargoQueryPort.findAvailableCargo().map { toCargoDTO(it) }
    }

    private fun toCargoDTO(cargo: Cargo) =
        CargoDTO(
            id = cargo.id,
            name = cargo.name,
            weight = cargo.weight
        )
}
