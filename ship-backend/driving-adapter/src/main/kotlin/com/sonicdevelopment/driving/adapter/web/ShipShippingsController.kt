package com.sonicdevelopment.driving.adapter.web

import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.ports.driving.shipping.ShippingInformationPort
import com.sonicdevelopment.domain.ports.driving.shipping.ShippingManagementPort
import com.sonicdevelopment.driving.adapter.web.mapper.toShipDetailResponse
import com.sonicdevelopment.driving.adapter.web.responsemodel.ShipDetailResponse
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
        return shippingInformationPort.getShipping(ShipId(shipId))?.let {
            toShipDetailResponse(it)
        } ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND, "Unable to find resource"
        )
    }

    @PostMapping("/{shipId}/shippings")
    fun createShipping(@PathVariable("shipId") shipId: UUID): ShipDetailResponse {
        return shippingManagementPort.createShipping(ShipId(shipId))?.let {
            toShipDetailResponse(it)
        } ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND, "Unable to find resource"
        )
    }

    @PutMapping("/{shipId}/shippings")
    fun releaseShipping(@PathVariable("shipId") shipId: UUID): ShipDetailResponse {
        return shippingManagementPort.releaseShipping(ShipId(shipId))?.let {
            toShipDetailResponse(it)
        } ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND, "Unable to find resource"
        )
    }
}