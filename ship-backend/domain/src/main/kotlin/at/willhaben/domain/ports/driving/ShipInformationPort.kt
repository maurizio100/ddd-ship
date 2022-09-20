package at.willhaben.domain.ports.driving

interface ShipInformationPort {
    fun getAllShips(): List<ShipDTO>
    fun getShipDetails(shipId: Long): ShipDetailDTO?
}
