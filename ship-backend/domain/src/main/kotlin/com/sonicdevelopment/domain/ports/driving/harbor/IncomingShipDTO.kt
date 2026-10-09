package com.sonicdevelopment.domain.ports.driving.harbor

import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.ports.driving.cargo.CargoDTO

/**
 * An Incoming Ship as the harbor management page lists it: its Cargo aboard, one entry per Cargo instance,
 * and its Delivery Price at this Harbor, `null` while a Cargo aboard has no Price here.
 */
data class IncomingShipDTO(
    val id: ShipId,
    val name: String,
    val arrivedFrom: String?,
    val cargo: List<CargoDTO>,
    val deliveryPrice: Money?,
)
