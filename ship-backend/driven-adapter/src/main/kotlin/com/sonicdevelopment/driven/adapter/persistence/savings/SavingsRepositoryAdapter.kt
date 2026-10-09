package com.sonicdevelopment.driven.adapter.persistence.savings

import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.domain.ports.driven.SavingsRepositoryPort
import org.springframework.stereotype.Component

@Component
class SavingsRepositoryAdapter(
    private val savingsPersistenceEntityRepository: SavingsPersistenceEntityRepository
) : SavingsRepositoryPort {

    /** The single row of `savings`, seeded with the Starting Savings by `V15`. */
    override fun getSavings(): Money = Money(savingsPersistenceEntityRepository.findAll().single().savingsAmount)

    override fun pay(amount: Money): Boolean = TODO("STORY-025")
}
