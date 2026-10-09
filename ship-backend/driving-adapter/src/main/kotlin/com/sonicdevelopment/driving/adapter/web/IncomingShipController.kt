package com.sonicdevelopment.driving.adapter.web

import com.sonicdevelopment.domain.ports.driving.harbor.HarborInformationPort
import com.sonicdevelopment.domain.ports.driving.harbor.IncomingShipDTO
import com.sonicdevelopment.driving.adapter.web.responsemodel.CargoResponse
import com.sonicdevelopment.driving.adapter.web.responsemodel.IncomingShipResponse
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/** The Incoming Ships of this Harbor, for the harbor management page. */
@RestController
@CrossOrigin(origins = [])
@RequestMapping("/web/incoming-ships")
class IncomingShipController(
    private val harborInformationPort: HarborInformationPort
) {
    @GetMapping
    fun getIncomingShips(): List<IncomingShipResponse> =
        harborInformationPort.getIncomingShips().map { toIncomingShipResponse(it) }

    private fun toIncomingShipResponse(incomingShip: IncomingShipDTO) =
        IncomingShipResponse(
            shipId = incomingShip.id.id,
            name = incomingShip.name,
            arrivedFrom = incomingShip.arrivedFrom,
            cargo = incomingShip.cargo.map { CargoResponse(id = it.id.id, name = it.name, weight = it.weight) },
            deliveryPrice = incomingShip.deliveryPrice?.toDecimalString(),
        )
}
