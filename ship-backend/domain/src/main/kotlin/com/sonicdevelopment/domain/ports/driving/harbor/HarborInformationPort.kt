package com.sonicdevelopment.domain.ports.driving.harbor

interface HarborInformationPort {
    fun getKnownHarbors(): KnownHarborsDTO
}
