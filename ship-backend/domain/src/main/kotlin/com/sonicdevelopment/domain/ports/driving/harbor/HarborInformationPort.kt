package com.sonicdevelopment.domain.ports.driving.harbor

import com.sonicdevelopment.domain.model.values.Money

interface HarborInformationPort {
    fun getKnownHarbors(): KnownHarborsDTO

    /** The Savings of this Harbor: the money it holds. */
    fun getSavings(): Money
}
