package com.sonicdevelopment.driving.adapter.web.responsemodel

import java.util.UUID

/**
 * An Incoming Ship on the harbor management page. [cargo] has one entry per Cargo instance aboard;
 * [deliveryPrice] is a decimal string with two decimals (ADR-0008), `null` when a Cargo aboard has no Price here.
 */
data class IncomingShipResponse(
    val shipId: UUID,
    val name: String,
    val arrivedFrom: String?,
    val cargo: List<CargoResponse>,
    val deliveryPrice: String?,
)
