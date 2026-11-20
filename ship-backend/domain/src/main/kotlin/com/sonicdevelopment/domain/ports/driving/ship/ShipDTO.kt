package com.sonicdevelopment.domain.ports.driving.ship

import com.sonicdevelopment.domain.model.enums.ShipClass
import com.sonicdevelopment.domain.model.enums.ShippingState
import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.domain.model.values.ShipId

data class ShipDTO(
    val id: ShipId,
    val name: String,
    val catain: String,
    val shippingState: ShippingState?,
    val arrivedFrom: String? = null,
    val incoming: Boolean = false,
    /** The Harbor where the ship was registered; never changes. */
    val homeHarbor: String,
    /** The Earnings the ship carries until it reaches its Home Harbor. */
    val earnings: Money = Money.dollars(0),
    /** Whether the ship is an Imprisoned Ship, or sails home as one. */
    val imprisoned: Boolean = false,
    /** The ship's Ship Class, which sets its Crew Capacity. */
    val shipClass: ShipClass = ShipClass.SLOOP,
    /** The Crew aboard. */
    val crew: List<CrewMemberDTO> = emptyList(),
) {
}
