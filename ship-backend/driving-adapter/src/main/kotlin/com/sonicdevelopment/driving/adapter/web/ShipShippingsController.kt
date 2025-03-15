package com.sonicdevelopment.driving.adapter.web

import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.model.values.ShippingId
import com.sonicdevelopment.domain.ports.driving.ship.ShipDetailDTO
import com.sonicdevelopment.domain.ports.driving.shipping.ShippingInformationPort
import com.sonicdevelopment.domain.ports.driving.shipping.ShippingManagementPort
import com.sonicdevelopment.driving.adapter.web.mapper.toShipDetailResponse
import com.sonicdevelopment.driving.adapter.web.responsemodel.CargoResponse
import com.sonicdevelopment.driving.adapter.web.responsemodel.ShipDetailResponse
import com.sonicdevelopment.driving.adapter.web.responsemodel.ShippingResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.*

@RestController
@CrossOrigin(origins = [])
@RequestMapping("/web/ships")
class ShipShippingsController(
    private val shippingManagementPort: ShippingManagementPort,
    private val shippingInformationPort: ShippingInformationPort
) {

    @GetMapping("/{shipId}/shippings")
    fun getActiveShipping(@PathVariable("shipId") shipId: UUID): ShipDetailResponse {
        throw ResponseStatusException(
            HttpStatus.NOT_FOUND, "Unable to find resource"
        )
        /*
        return shippingInformationPort.getAllShipping(ShipId(shipId))?.let {
            toShipDetailResponse(it)
        } ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND, "Unable to find resource"
        )
         */
    }

    @GetMapping("/{shipId}/shippings/{shippingsId}")
    fun getShippingDetails(@PathVariable("shipId") shipId: UUID, shippingsId: UUID): ShippingResponse {
        return shippingInformationPort.getShipping(ShipId(shipId), ShippingId(shippingsId))?.let {
            toShippingResponse(it)
        } ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND, "Unable to find resource"
        )
    }

    @PostMapping("/{shipId}/shippings")
    fun createShipping(@PathVariable("shipId") shipId: UUID): ShippingResponse {
        return shippingManagementPort.createShipping(ShipId(shipId))?.let {
            toShippingResponse(it)
        } ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND, "Unable to find resource"
        )
    }

    @PutMapping("/{shipId}/shippings")
    fun releaseShipping(@PathVariable("shipId") shipId: UUID): ShippingResponse {
        return shippingManagementPort.releaseShipping(ShipId(shipId))?.let {
            toShippingResponse(it)
        } ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND, "Unable to find resource"
        )
    }

    private fun toShippingResponse(shipDetailDTO: ShipDetailDTO) =
        ShippingResponse(
            id = UUID.randomUUID(),
            name = shipDetailDTO.name,
            sailorsCode = "blabla",
            cargo = shipDetailDTO.cargo.map {
                CargoResponse(id = it.id.id, name = it.name, weight = it.weight)
            }
        )
}