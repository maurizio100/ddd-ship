package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.model.values.Money

/**
 * The Fine: what a Home Harbor pays from its Savings to free its Imprisoned Ship. A fixed amount, the same for
 * every ship, shared by the Imprisoned Ships list and the payment, so that the Fine listed and the Fine paid can
 * never differ.
 */
object Fine {
    val AMOUNT: Money = Money.of("50.00")
}
