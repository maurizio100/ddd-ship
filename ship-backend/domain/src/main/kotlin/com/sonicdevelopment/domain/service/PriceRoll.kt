package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.model.values.Money
import kotlin.random.Random

/**
 * Rolls a Price for a Cargo: whole dollars from [LOWEST_PRICE] to [HIGHEST_PRICE]. The [random] source is
 * passed in, so tests can control the roll.
 */
class PriceRoll(private val random: Random) {

    fun roll(): Money = TODO("STORY-024")

    companion object {
        const val LOWEST_PRICE = 30
        const val HIGHEST_PRICE = 60
    }
}
