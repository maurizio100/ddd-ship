package com.sonicdevelopment.driven.adapter.persistence.savings

import jakarta.persistence.*
import java.math.BigDecimal

/** The Savings of this Harbor: the single row of `savings`. */
@Entity
@Table(name = "savings")
class SavingsPersistenceEntity(
    @Id
    @GeneratedValue
    @Column(name = "id")
    var id: Long,

    @Column(name = "savings_amount")
    var savingsAmount: BigDecimal
)
