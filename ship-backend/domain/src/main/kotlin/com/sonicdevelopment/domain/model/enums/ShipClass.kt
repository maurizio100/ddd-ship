package com.sonicdevelopment.domain.model.enums

/** The size of a ship, which sets its Crew Capacity. */
enum class ShipClass(val crewCapacity: Int) {
    SLOOP(2), BRIG(4), GALLEON(6)
}
