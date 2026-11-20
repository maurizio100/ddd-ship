package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.model.Recruit
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.RecruitId
import java.util.UUID
import kotlin.random.Random

/** How many Recruits a Harbor offers. */
const val RECRUIT_POOL_SIZE = 5

/**
 * Generates the Recruits of one Harbor as a pure function of its Harbor Name and an ordinal (ADR-0003, ADR-0009):
 * no coordination between Harbors, and a re-opening Harbor regenerates the same Recruits. Ordinal n is the n-th
 * Recruit ever generated at the Harbor, so the next unused ordinal is a Recruit never offered before.
 */
class RecruitPoolGenerator(private val harbor: HarborName) {

    private val seed = Random(harbor.name.hashCode().toLong() * 31 + harbor.name.length)
    private val names = NAMES.shuffled(seed)
    private val traits = TRAITS.shuffled(seed)

    fun recruit(ordinal: Int): Recruit {
        require(ordinal >= 0) { "A Recruit ordinal must not be negative" }
        val round = ordinal / names.size
        val name = names[ordinal % names.size] + if (round == 0) "" else " " + roman(round + 1)
        return Recruit(
            id = RecruitId(UUID.nameUUIDFromBytes("${harbor.name}/$ordinal".toByteArray())),
            name = name,
            trait = traits[ordinal % traits.size]
        )
    }

    private fun roman(number: Int): String {
        var rest = number
        return buildString {
            for ((value, symbol) in ROMAN) while (rest >= value) { append(symbol); rest -= value }
        }
    }

    private companion object {
        val ROMAN = listOf(1000 to "M", 900 to "CM", 500 to "D", 400 to "CD", 100 to "C", 90 to "XC", 50 to "L",
            40 to "XL", 10 to "X", 9 to "IX", 5 to "V", 4 to "IV", 1 to "I")

        val NAMES = listOf(
            "Mittens", "Patch", "Saffron", "Biscuit", "Pepper", "Whiskers", "Marmalade", "Smudge", "Clove", "Ginger",
            "Pickle", "Soot", "Truffle", "Juniper", "Nutmeg", "Cinder", "Maple", "Tansy", "Bramble", "Fennel",
            "Olive", "Pudding", "Sorrel", "Thistle", "Mackerel", "Barnacle", "Cobweb", "Dumpling", "Ember", "Figgy",
            "Gizmo", "Haddock", "Inkwell", "Jasper", "Kipper", "Lavender", "Muffin", "Nettle", "Oyster", "Parsnip"
        )

        val TRAITS = listOf(
            "Fearless", "Sharp-eyed", "Sleepy", "Sticky-pawed", "Seasick", "Loyal", "Mischievous", "Fast",
            "Stubborn", "Curious", "Greedy", "Calm"
        )
    }
}
