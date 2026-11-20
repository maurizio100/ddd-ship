package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.model.values.Money

/**
 * The Hiring Fee: what the Savings pay to Hire a Recruit. A fixed amount, the same at every Harbor and for every
 * Recruit, shared by the Recruit Pool list and the Hire, so that the fee listed and the fee paid can never differ.
 */
object HiringFee {
    val AMOUNT: Money = Money.of("50.00")
}
