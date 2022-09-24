package at.willhaben.driving.adapter.web

import at.willhaben.domain.ports.driving.cargo.CargoDTO
import at.willhaben.domain.ports.driving.cargo.CargoInformationPort
import at.willhaben.driving.adapter.web.responsemodel.CargoResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/web/cargos")
class CargoController(
    private val cargoInformationPort: CargoInformationPort
) {
    @GetMapping
    fun getCargos(): List<CargoResponse> =
        cargoInformationPort.getAvailableCargo().map {toCargoResponse(it)}

    private fun toCargoResponse(cargo: CargoDTO) =
        CargoResponse(
            id = cargo.id,
            name = cargo.name,
            weight = cargo.weight
        )
}
