package at.willhaben.domain.ports.driving

interface ShipInformationPort {
    fun getShipDetails(shipId: Long): ShipDetailDTO
}
