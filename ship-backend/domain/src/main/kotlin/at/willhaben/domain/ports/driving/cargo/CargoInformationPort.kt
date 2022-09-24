package at.willhaben.domain.ports.driving.cargo

interface CargoInformationPort {
    fun getAvailableCargo(): CargoDTO
}
