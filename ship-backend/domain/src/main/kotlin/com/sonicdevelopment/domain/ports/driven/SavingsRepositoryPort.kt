package com.sonicdevelopment.domain.ports.driven

import com.sonicdevelopment.domain.model.values.Money

/** The Savings of this Harbor: the money it holds. */
interface SavingsRepositoryPort {
    fun getSavings(): Money
}
