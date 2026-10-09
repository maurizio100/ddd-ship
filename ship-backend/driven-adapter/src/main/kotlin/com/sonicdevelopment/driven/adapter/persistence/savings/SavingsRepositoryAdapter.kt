package com.sonicdevelopment.driven.adapter.persistence.savings

import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.domain.ports.driven.SavingsRepositoryPort
import org.springframework.stereotype.Component

@Component
class SavingsRepositoryAdapter(
    private val savingsPersistenceEntityRepository: SavingsPersistenceEntityRepository
) : SavingsRepositoryPort {

    override fun getSavings(): Money = TODO("STORY-024")
}
