package com.sonicdevelopment.application.acceptance.fixtures

import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.domain.ports.driven.SavingsRepositoryPort
import java.math.BigDecimal

/**
 * The Savings in memory. A fresh Harbor holds the Starting Savings, standing in for the seed of `V15`.
 * [pay] mirrors the adapter's conditional update: it takes the amount only when the Savings cover it.
 * [receive] adds the amount, as the adapter's atomic update does.
 */
class InMemorySavings : SavingsRepositoryPort {

    private var savings: String = STARTING_SAVINGS

    @Synchronized
    override fun getSavings(): Money = Money(BigDecimal(savings))

    @Synchronized
    override fun pay(amount: Money): Boolean {
        val held = BigDecimal(savings)
        if (held < amount.amount) return false
        savings = (held - amount.amount).toPlainString()
        return true
    }

    @Synchronized
    override fun receive(amount: Money) {
        savings = (BigDecimal(savings) + amount.amount).toPlainString()
    }

    @Synchronized
    fun setSavings(decimal: String) {
        savings = decimal
    }

    @Synchronized
    fun reset() {
        savings = STARTING_SAVINGS
    }
}
