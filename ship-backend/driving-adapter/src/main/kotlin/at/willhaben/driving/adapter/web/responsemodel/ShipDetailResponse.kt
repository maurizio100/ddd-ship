package at.willhaben.driving.adapter.web.responsemodel

data class ShipDetailResponse(
    val id: Long,
    val name: String,
    val cargo: List<CargoResponse> = listOf(),
    val weight: Float = 0.0F,
    val maxweight: Float = 0.0F,
)
