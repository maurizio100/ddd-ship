package at.willhaben.domain.ports.driving.shipping

import at.willhaben.domain.ports.driving.cargo.CargoDTO

data class ShippingInformationDTO(
    val shippingId: Long,
    val shipName: String,
    val cargo: CargoDTO,
    val sailorsCode: String
)
