package com.sonicdevelopment.domain.model.values

import java.util.UUID

enum class FurColour { GINGER, GREY, BLACK, CREAM, BROWN, WHITE }
enum class CoatPattern { PLAIN, TABBY, TUXEDO, SPOTTED }
enum class EyeColour { GREEN, AMBER, BLUE }
enum class Accessory { NONE, EYE_PATCH, BANDANA, EARRING }

/** The look of a Recruit and of the Crew Member it becomes (Recruit Portrait). */
data class Portrait(val fur: FurColour, val coat: CoatPattern, val eyes: EyeColour, val accessory: Accessory) {
    companion object {
        /** Pure function of the UUID's bits (never `hashCode` or `Random`): the same at every Harbor and JVM. */
        fun of(id: UUID): Portrait {
            val hi = id.mostSignificantBits
            val lo = id.leastSignificantBits
            return Portrait(
                fur = pick(FurColour.entries, hi ushr 32),
                coat = pick(CoatPattern.entries, hi and 0xFFFFFFFFL),
                eyes = pick(EyeColour.entries, lo ushr 32),
                accessory = pick(Accessory.entries, lo and 0xFFFFFFFFL),
            )
        }

        private fun <T> pick(values: List<T>, bits: Long): T = values[Math.floorMod(bits, values.size.toLong()).toInt()]
    }
}
