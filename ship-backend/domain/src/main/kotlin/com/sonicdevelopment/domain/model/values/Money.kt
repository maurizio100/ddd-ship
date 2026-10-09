package com.sonicdevelopment.domain.model.values

import java.math.BigDecimal

/** Scaffold for STORY-024: an amount of dollars. Behaviour (scale 2, rejecting a third decimal, equality) comes with the implementation. */
class Money(val amount: BigDecimal) {

    fun toDecimalString(): String = TODO("STORY-024")

    companion object {
        fun dollars(whole: Int): Money = TODO("STORY-024")

        fun of(decimal: String): Money = TODO("STORY-024")
    }
}
