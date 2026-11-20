package com.sonicdevelopment.domain.model

import com.sonicdevelopment.domain.model.values.RecruitId

/** A cat a Harbor offers in its Recruit Pool: a UUID identity, a name and a trait. */
data class Recruit(val id: RecruitId, val name: String, val trait: String) {
    init {
        require(name.isNotBlank()) { "A Recruit's name must not be blank" }
        require(trait.isNotBlank()) { "A Recruit's trait must not be blank" }
    }
}
