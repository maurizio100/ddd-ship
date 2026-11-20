package com.sonicdevelopment.driving.adapter.web

import com.sonicdevelopment.domain.ports.driving.harbor.HarborInformationPort
import com.sonicdevelopment.driving.adapter.web.responsemodel.RecruitResponse
import com.sonicdevelopment.driving.adapter.web.responsemodel.toResponse
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin(origins = [])
@RequestMapping("/web/recruits")
class RecruitController(
    private val harborInformationPort: HarborInformationPort
) {
    @GetMapping
    fun getRecruitPool(): List<RecruitResponse> =
        harborInformationPort.getRecruitPool().map { RecruitResponse(it.id, it.name, it.trait, it.hiringFee.toDecimalString(), it.portrait.toResponse()) }
}
