package at.willhaben.domain.service

import at.willhaben.domain.ports.driving.cargo.CargoDTO
import at.willhaben.domain.ports.driving.cargo.CargoInformationPort

class CargoInformationService(): CargoInformationPort {
    override fun getAvailableCargo(): CargoDTO {
        TODO("Not yet implemented")
    }
}
