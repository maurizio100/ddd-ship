package com.sonicdevelopment.driving.adapter.web

import com.sonicdevelopment.domain.ports.driving.harbor.HarborInformationPort
import com.sonicdevelopment.driving.adapter.web.responsemodel.SavingsResponse
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin(origins = [])
@RequestMapping("/web/savings")
class SavingsController(
    private val harborInformationPort: HarborInformationPort
) {
    @GetMapping
    fun getSavings(): SavingsResponse = TODO("STORY-024")
}
