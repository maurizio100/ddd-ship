package com.sonicdevelopment.domain.model.values

import java.util.UUID

enum class FurColour { GINGER, GREY, BLACK, CREAM, BROWN, WHITE }
enum class CoatPattern { PLAIN, TABBY, TUXEDO, SPOTTED }
enum class EyeColour { GREEN, AMBER, BLUE }
enum class Accessory { NONE, EYE_PATCH, BANDANA, EARRING }

/** The look of a Recruit and of the Crew Member it becomes (Recruit Portrait). */
data class Portrait(val fur: FurColour, val coat: CoatPattern, val eyes: EyeColour, val accessory: Accessory) {
    companion object {
        fun of(id: UUID): Portrait = Portrait(FurColour.GINGER, CoatPattern.PLAIN, EyeColour.GREEN, Accessory.NONE)
    }
}
