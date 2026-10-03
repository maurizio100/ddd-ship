package com.sonicdevelopment.driving.adapter.web

import com.sonicdevelopment.domain.ports.driving.harbor.HarborInformationPort
import com.sonicdevelopment.driving.adapter.web.responsemodel.KnownHarborsResponse

class HarborController(
    private val harborInformationPort: HarborInformationPort
) {
    fun getKnownHarbors(): KnownHarborsResponse = TODO()
}
