package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.model.values.Money
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.random.Random

class PriceRollTest {

    /** A Random whose `nextInt(from, until)` returns what [pick] chooses and records the bounds it was asked for. */
    private class StubRandom(private val pick: (from: Int, until: Int) -> Int) : Random() {
        var askedFor: Pair<Int, Int>? = null

        override fun nextBits(bitCount: Int): Int = error("not used by PriceRoll")

        override fun nextInt(from: Int, until: Int): Int {
            askedFor = from to until
            return pick(from, until)
        }
    }

    @Test
    fun `the lowest roll is 30 dollars`() {
        PriceRoll(StubRandom { from, _ -> from }).roll() shouldBe Money.of("30.00")
    }

    @Test
    fun `the highest roll is 60 dollars`() {
        PriceRoll(StubRandom { _, until -> until - 1 }).roll() shouldBe Money.of("60.00")
    }

    @Test
    fun `asks the random source for a whole number from 30 to 60`() {
        val random = StubRandom { from, _ -> from }

        PriceRoll(random).roll()

        random.askedFor shouldBe (30 to 61)
    }

    @Test
    fun `every roll is whole dollars from 30 to 60`() {
        val priceRoll = PriceRoll(Random(7))
        val wholeDollars = (30..60).map { Money.dollars(it) }

        repeat(1000) {
            val price = priceRoll.roll()
            price shouldBeIn wholeDollars
            price.amount.remainder(BigDecimal.ONE).signum() shouldBe 0
        }
    }
}
