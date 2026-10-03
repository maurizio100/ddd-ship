package com.sonicdevelopment.driving.adapter.web

import com.sonicdevelopment.domain.ports.driving.harbor.HarborInformationPort
import com.sonicdevelopment.driving.adapter.web.responsemodel.KnownHarborsResponse
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin(origins = [])
@RequestMapping("/web/harbors")
class HarborController(
    private val harborInformationPort: HarborInformationPort
) {

    @GetMapping
    fun getKnownHarbors(): KnownHarborsResponse {
        val knownHarbors = harborInformationPort.getKnownHarbors()
        return KnownHarborsResponse(
            harborName = knownHarbors.harborName.name,
            knownHarbors = knownHarbors.knownHarbors.map { it.name }
        )
    }
}
