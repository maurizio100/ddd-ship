package com.sonicdevelopment.driving.adapter.web

import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.ports.driving.shipping.ShippingCreationDataDTO
import com.sonicdevelopment.domain.ports.driving.shipping.ShippingInformationDTO
import com.sonicdevelopment.domain.ports.driving.shipping.ShippingInformationPort
import com.sonicdevelopment.domain.ports.driving.shipping.ShippingManagementPort
import com.sonicdevelopment.driving.adapter.web.requestmodel.ShippingCreationRequest
import com.sonicdevelopment.driving.adapter.web.responsemodel.CargoResponse
import com.sonicdevelopment.driving.adapter.web.responsemodel.ShippingResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/web/shippings")
class ShippingController(
    private val shippingManagementPort: ShippingManagementPort,
    private val shippingInformationPort: ShippingInformationPort
) {
    @PostMapping
    fun createShipping(@RequestBody shippingCreationRequest: ShippingCreationRequest): ShippingResponse {
        val shippingId = shippingCreationRequest.shippingId ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource")
        val shipping = shippingManagementPort.createShipping(ShippingCreationDataDTO(ShipId(shippingId)))
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

    @GetMapping("/{shippingId}")
    fun getShipping(@PathVariable("shippingId") shippingId: Long): ShippingResponse {
        return shippingInformationPort.getShipping(shippingId)?.let {
            toShippingResponse(it)
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource")
    }
}
