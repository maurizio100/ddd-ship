package com.sonicdevelopment.domain.model.values

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class MoneyTest {

    @Test
    fun `whole dollars have two decimals`() {
        Money.dollars(42).toDecimalString() shouldBe "42.00"
        Money.dollars(42).amount shouldBe BigDecimal("42.00")
    }

    @Test
    fun `a decimal string with one decimal equals the same amount with two`() {
        Money.of("640.5") shouldBe Money.of("640.50")
        Money.of("640.5").toDecimalString() shouldBe "640.50"
    }

    @Test
    fun `an amount with three decimals is rejected, not rounded`() {
        shouldThrow<IllegalArgumentException> { Money.of("1.234") }
        shouldThrow<IllegalArgumentException> { Money(BigDecimal("1.005")) }
    }

    @Test
    fun `amounts with a different scale are equal and hash alike`() {
        val plain = Money(BigDecimal("1000"))
        val twoDecimals = Money(BigDecimal("1000.00"))

        plain shouldBe twoDecimals
        plain.hashCode() shouldBe twoDecimals.hashCode()
        plain.toDecimalString() shouldBe "1000.00"
        plain shouldNotBe Money.of("1000.01")
    }

    @Test
    fun `trailing zeros beyond two decimals are not a third decimal`() {
        Money(BigDecimal("12.500")).toDecimalString() shouldBe "12.50"
    }

    @Test
    fun `times multiplies exactly and keeps two decimals`() {
        (Money.of("50.00") * 2) shouldBe Money.of("100.00")
        (Money.of("50.00") * 2).toDecimalString() shouldBe "100.00"
        (Money.of("33.33") * 3).toDecimalString() shouldBe "99.99"
        (Money.of("50.00") * 0).toDecimalString() shouldBe "0.00"
    }

    @Test
    fun `plus adds exactly to the cent`() {
        (Money.of("40.00") + Money.of("40.00") + Money.of("35.00")).toDecimalString() shouldBe "115.00"
        (Money.of("0.10") + Money.of("0.20")) shouldBe Money.of("0.30")
        (Money.dollars(0) + Money.of("12.34")).toDecimalString() shouldBe "12.34"
    }
}
