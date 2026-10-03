package com.sonicdevelopment.domain.model.values

/** The unique name a Harbor is known by among all Harbors. */
data class HarborName(val name: String) {
    init {
        require(name.isNotBlank()) { "A Harbor Name must not be blank" }
    }
}
