package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.ports.driven.CargoQueryPort
import com.sonicdevelopment.domain.ports.driven.StockRepositoryPort
import com.sonicdevelopment.domain.ports.driving.cargo.AvailableCargoDTO
import com.sonicdevelopment.domain.ports.driving.cargo.CargoInformationPort
import org.springframework.stereotype.Service

@Service
class CargoInformationService(
    private val cargoQueryPort: CargoQueryPort,
    private val stockRepositoryPort: StockRepositoryPort
): CargoInformationPort {
    override fun getAvailableCargo(): List<AvailableCargoDTO> = TODO("STORY-004")
}
