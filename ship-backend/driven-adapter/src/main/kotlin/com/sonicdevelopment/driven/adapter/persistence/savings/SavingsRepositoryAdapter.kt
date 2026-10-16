package com.sonicdevelopment.driven.adapter.persistence.savings

import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.domain.ports.driven.SavingsRepositoryPort
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class SavingsRepositoryAdapter(
    private val savingsPersistenceEntityRepository: SavingsPersistenceEntityRepository
) : SavingsRepositoryPort {

    /** The single row of `savings`, seeded with the Starting Savings by `V15`. */
    override fun getSavings(): Money = Money(savingsPersistenceEntityRepository.findAll().single().savingsAmount)

    @Transactional(propagation = Propagation.MANDATORY)
    override fun pay(amount: Money): Boolean = savingsPersistenceEntityRepository.pay(amount.amount) == 1

    @Transactional(propagation = Propagation.MANDATORY)
    override fun receive(amount: Money) {
        val updated = savingsPersistenceEntityRepository.receive(amount.amount)
        check(updated == 1) { "The Savings are a single row, but $updated rows received $amount" }
    }
}
