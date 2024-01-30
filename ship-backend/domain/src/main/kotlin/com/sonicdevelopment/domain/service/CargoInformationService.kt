package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.model.Cargo
import com.sonicdevelopment.domain.ports.driven.CargoQueryPort
import com.sonicdevelopment.domain.ports.driving.cargo.CargoDTO
import com.sonicdevelopment.domain.ports.driving.cargo.CargoInformationPort
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
