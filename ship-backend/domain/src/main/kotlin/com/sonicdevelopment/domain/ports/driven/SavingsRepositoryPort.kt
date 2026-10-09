package com.sonicdevelopment.domain.ports.driven

import com.sonicdevelopment.domain.model.values.Money

/** The Savings of this Harbor: the money it holds. */
interface SavingsRepositoryPort {
    fun getSavings(): Money

    /**
     * Takes [amount] out of the Savings in one atomic step. Returns `false`, and changes nothing, when
     * the Savings hold less than [amount]. Runs in the caller's transaction.
     */
    fun pay(amount: Money): Boolean
}
