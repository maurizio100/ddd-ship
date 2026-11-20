package com.sonicdevelopment.domain.model

import com.sonicdevelopment.domain.model.values.CrewMemberId

/** A Crew Member aboard a ship; keeps the identity, name and trait of the Recruit it was Hired as. */
data class CrewMember(val id: CrewMemberId, val name: String, val trait: String) {
    init {
        require(name.isNotBlank()) { "A Crew Member has a name" }
        require(trait.isNotBlank()) { "A Crew Member has a trait" }
    }

    companion object {
        fun hired(recruit: Recruit) = CrewMember(CrewMemberId(recruit.id.id), recruit.name, recruit.trait)
    }
}
