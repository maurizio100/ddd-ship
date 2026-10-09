package com.sonicdevelopment.driven.adapter.persistence.savings

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.math.BigDecimal

interface SavingsPersistenceEntityRepository : JpaRepository<SavingsPersistenceEntity, Long> {

    /**
     * Takes [amount] out of the Savings if they hold at least that much; returns the number of updated
     * rows (1 or 0). The `WHERE` is re-checked after a concurrent writer commits, so two payments cannot
     * together take the Savings below 0.
     */
    @Modifying
    @Query(
        nativeQuery = true,
        value = "UPDATE savings SET savings_amount = savings_amount - :amount WHERE savings_amount >= :amount"
    )
    fun pay(@Param("amount") amount: BigDecimal): Int
}
