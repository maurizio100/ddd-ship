package com.sonicdevelopment.driving.adapter.web

import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.ports.driving.harbor.HarborInformationPort
import com.sonicdevelopment.domain.ports.driving.harbor.ImprisonedShipDTO
import com.sonicdevelopment.domain.ports.driving.shipping.FinePaymentDTO
import com.sonicdevelopment.domain.ports.driving.shipping.ImprisonmentManagementPort
import com.sonicdevelopment.driving.adapter.web.responsemodel.FinePaymentResponse
import com.sonicdevelopment.driving.adapter.web.responsemodel.ImprisonedShipResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

/** The Imprisoned Ships at this, their Home, Harbor, for the Imprisoned Ships page and the fleet. */
@RestController
@CrossOrigin(origins = [])
@RequestMapping("/web/imprisoned-ships")
class ImprisonedShipController(
    private val harborInformationPort: HarborInformationPort,
    private val imprisonmentManagementPort: ImprisonmentManagementPort,
) {
    @GetMapping
    fun getImprisonedShips(): List<ImprisonedShipResponse> =
        harborInformationPort.getImprisonedShips().map { toImprisonedShipResponse(it) }

    /**
     * Pays the Fine from the Savings and frees the ship; answers with the Fine paid and the Savings after. The
     * client reads the Imprisoned Ships and the fleet back from their GETs.
     */
    @PostMapping("/{shipId}/fine-payment")
    fun payFine(@PathVariable shipId: UUID): FinePaymentResponse =
        imprisonmentManagementPort.payFine(ShipId(shipId))?.let { toFinePaymentResponse(it) }
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource")

    private fun toImprisonedShipResponse(imprisonedShip: ImprisonedShipDTO) =
        ImprisonedShipResponse(
            shipId = imprisonedShip.id.id,
            name = imprisonedShip.name,
            catain = imprisonedShip.catain,
            homeHarbor = imprisonedShip.homeHarbor.name,
            fine = imprisonedShip.fine.toDecimalString(),
            savingsCoverFine = imprisonedShip.savingsCoverFine,
        )

    private fun toFinePaymentResponse(payment: FinePaymentDTO) =
        FinePaymentResponse(
            shipId = payment.shipId.id,
            name = payment.shipName,
            fine = payment.fine.toDecimalString(),
            savings = payment.savings.toDecimalString(),
        )
}
