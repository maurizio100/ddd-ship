package com.sonicdevelopment.domain.model.values

import java.math.BigDecimal

/**
 * An amount of dollars, exact to the cent (ADR-0008): always held at scale 2, never a float. An amount
 * with more than two decimals is rejected, never rounded. Two amounts are equal when their value is,
 * whatever scale they were given in (`1000` equals `1000.00`).
 */
class Money(amount: BigDecimal) {

    val amount: BigDecimal

    init {
        require(amount.stripTrailingZeros().scale() <= SCALE) { "Money has at most $SCALE decimals, not $amount" }
        this.amount = amount.setScale(SCALE)
    }

    /** The amount as a decimal string with two decimals, e.g. `"1000.00"`. */
    fun toDecimalString(): String = amount.toPlainString()

    /** This amount [quantity] times, exact to the cent. */
    operator fun times(quantity: Int): Money = Money(amount.multiply(BigDecimal.valueOf(quantity.toLong())))

    /** This amount and [other] together, exact to the cent. */
    operator fun plus(other: Money): Money = Money(amount.add(other.amount))

    override fun equals(other: Any?): Boolean = other is Money && other.amount == amount

    override fun hashCode(): Int = amount.hashCode()

    override fun toString(): String = "${toDecimalString()} $"

    companion object {
        private const val SCALE = 2

        fun dollars(whole: Int): Money = Money(BigDecimal.valueOf(whole.toLong()))

        /** Reads a decimal string such as `"640.50"` or `"640.5"`. */
        fun of(decimal: String): Money = Money(BigDecimal(decimal))
    }
}
