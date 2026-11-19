package com.sonicdevelopment.coastguard

/** The goods the Coast Guard counts, matched on Cargo name. */
object Contraband {
    val names: Set<String> = setOf("Ale", "Rum", "Wine", "Tobacco")

    fun carriesContraband(cargoNames: List<String>): Boolean = cargoNames.any { it in names }
}
