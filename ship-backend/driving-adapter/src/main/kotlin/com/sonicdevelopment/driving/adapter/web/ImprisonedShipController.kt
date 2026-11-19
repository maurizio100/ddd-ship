package com.sonicdevelopment.driving.adapter.web

import com.sonicdevelopment.domain.ports.driving.harbor.HarborInformationPort
import com.sonicdevelopment.domain.ports.driving.shipping.ImprisonmentManagementPort
import com.sonicdevelopment.driving.adapter.web.responsemodel.FinePaymentResponse
import com.sonicdevelopment.driving.adapter.web.responsemodel.ImprisonedShipResponse
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
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
    fun getImprisonedShips(): List<ImprisonedShipResponse> = TODO("STORY-053")

    @PostMapping("/{shipId}/fine-payment")
    fun payFine(@PathVariable shipId: UUID): FinePaymentResponse = TODO("STORY-053")
}
