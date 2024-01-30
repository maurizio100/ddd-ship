package com.sonicdevelopment.domain.ports.driving.cargo

interface CargoInformationPort {
    fun getAvailableCargo(): List<CargoDTO>
}
