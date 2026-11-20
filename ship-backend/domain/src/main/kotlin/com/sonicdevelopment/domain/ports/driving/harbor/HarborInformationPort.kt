package com.sonicdevelopment.domain.ports.driving.harbor

import com.sonicdevelopment.domain.model.values.Money

interface HarborInformationPort {
    fun getKnownHarbors(): KnownHarborsDTO

    /** The Savings of this Harbor: the money it holds. */
    fun getSavings(): Money

    /**
     * The Incoming Ships in this Harbor's fleet, each with its Cargo aboard and its Delivery Price: the sum of
     * this Harbor's Prices of every Cargo aboard, `null` if one of them has no Price here.
     */
    fun getIncomingShips(): List<IncomingShipDTO>

    /**
     * The Imprisoned Ships at this, their Home, Harbor and in port, each with its Fine and whether the Savings
     * cover it. A ship sent home imprisoned is not listed while it is at sea.
     */
    fun getImprisonedShips(): List<ImprisonedShipDTO>

    /** The Recruits this Harbor offers: its Recruit Pool, in the order they were generated. */
    fun getRecruitPool(): List<RecruitDTO>
}
