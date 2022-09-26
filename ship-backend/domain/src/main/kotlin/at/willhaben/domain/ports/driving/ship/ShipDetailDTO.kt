package at.willhaben.domain.ports.driving.ship

import at.willhaben.domain.ports.driving.cargo.CargoDTO

data class ShipDetailDTO(
    val id: Long,
    val name: String,
    val cargo: List<CargoDTO>,
    val actualWeight: Float,
    val maxWeight: Float
) {
}
