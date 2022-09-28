package at.willhaben.driving.adapter.web

import at.willhaben.domain.ports.driving.shipping.ShippingCreationDataDTO
import at.willhaben.domain.ports.driving.shipping.ShippingInformationDTO
import at.willhaben.domain.ports.driving.shipping.ShippingManagementPort
import at.willhaben.driving.adapter.web.requestmodel.ShippingCreationRequest
import at.willhaben.driving.adapter.web.responsemodel.CargoResponse
import at.willhaben.driving.adapter.web.responsemodel.ShippingResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/web/shippings")
@CrossOrigin(origins = ["http://localhost:4200"])
class ShippingController(
    private val shippingManagementPort: ShippingManagementPort
) {
    @PostMapping
    fun createShipping(@RequestBody shippingCreationRequest: ShippingCreationRequest): ShippingResponse {
        val shippingId = shippingCreationRequest.shippingId ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource")
        val shipping = shippingManagementPort.createShipping(ShippingCreationDataDTO(shippingId))
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource")

        return toShippingResponse(shipping)
    }

    private fun toShippingResponse(shippingInformationDTO: ShippingInformationDTO) =
        ShippingResponse(
            id = shippingInformationDTO.shippingId,
            name = shippingInformationDTO.shipName,
            sailorsCode = shippingInformationDTO.sailorsCode,
            cargo = shippingInformationDTO.cargo.map {
                CargoResponse(id = it.id, name = it.name, weight = it.weight)
            }
        )
}
