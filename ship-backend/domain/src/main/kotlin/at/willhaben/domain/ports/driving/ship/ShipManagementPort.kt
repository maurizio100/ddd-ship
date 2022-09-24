package at.willhaben.domain.ports.driving.ship

interface ShipManagementPort {
    fun createShip(shipCreationData: ShipCreationDataDTO): ShipDTO
    fun deleteShip(shipId: Long)
    fun updateShip(shipId: Long, shipUpdateData: ShipUpdateDataDTO): ShipDTO?
}
