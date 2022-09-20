package at.willhaben.domain.ports.driving

interface ShipManagementPort {
    fun createShip(shipCreationData: ShipCreationDataDTO): ShipDTO
    fun deleteShip(shipId: Long)
}
