package com.sonicdevelopment.driving.adapter.web

import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.ports.driving.harbor.HarborInformationPort
import com.sonicdevelopment.domain.ports.driving.harbor.IncomingShipDTO
import com.sonicdevelopment.domain.ports.driving.harbor.IncomingShipManagementPort
import com.sonicdevelopment.driving.adapter.web.responsemodel.CargoResponse
import com.sonicdevelopment.driving.adapter.web.responsemodel.IncomingShipResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

/** The Incoming Ships of this Harbor, for the harbor management page. */
@RestController
@CrossOrigin(origins = [])
@RequestMapping("/web/incoming-ships")
class IncomingShipController(
    private val harborInformationPort: HarborInformationPort,
    private val incomingShipManagementPort: IncomingShipManagementPort,
) {
    @GetMapping
    fun getIncomingShips(): List<IncomingShipResponse> =
        harborInformationPort.getIncomingShips().map { toIncomingShipResponse(it) }

    /**
     * Unloads the Incoming Ship: pays its Delivery Price and puts its Cargo into the Stock. The client reads the
     * Incoming Ships, the Stock and the Savings back from their GETs.
     */
    @PostMapping("/{shipId}/unloading")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun unloadIncomingShip(@PathVariable shipId: UUID) {
        incomingShipManagementPort.unloadIncomingShip(ShipId(shipId))
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource")
    }

    /**
     * Refuses the Incoming Ship: it sails back to its Home Harbor with its Cargo aboard; nothing is paid and the
     * Stock is unchanged. The client reads the Incoming Ships back from their GET.
     */
    @PostMapping("/{shipId}/refusal")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun refuseIncomingShip(@PathVariable shipId: UUID) {
        incomingShipManagementPort.refuseIncomingShip(ShipId(shipId))
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource")
    }

    private fun toIncomingShipResponse(incomingShip: IncomingShipDTO) =
        IncomingShipResponse(
            shipId = incomingShip.id.id,
            name = incomingShip.name,
            arrivedFrom = incomingShip.arrivedFrom,
            cargo = incomingShip.cargo.map { CargoResponse(id = it.id.id, name = it.name, weight = it.weight) },
            deliveryPrice = incomingShip.deliveryPrice?.toDecimalString(),
        )
}
