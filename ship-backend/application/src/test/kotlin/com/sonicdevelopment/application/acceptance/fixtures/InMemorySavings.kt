package com.sonicdevelopment.application.acceptance.fixtures

import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.domain.ports.driven.SavingsRepositoryPort
import java.math.BigDecimal

/** The Savings in memory. A fresh Harbor holds the Starting Savings, standing in for the seed of `V15`. */
class InMemorySavings : SavingsRepositoryPort {

    private var savings: String = STARTING_SAVINGS

    @Synchronized
    override fun getSavings(): Money = Money(BigDecimal(savings))

    @Synchronized
    fun setSavings(decimal: String) {
        savings = decimal
    }

    @Synchronized
    fun reset() {
        savings = STARTING_SAVINGS
    }
}
