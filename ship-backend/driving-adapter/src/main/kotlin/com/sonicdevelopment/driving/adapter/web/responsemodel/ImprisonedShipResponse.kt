package com.sonicdevelopment.driving.adapter.web.responsemodel

import java.util.UUID

/**
 * An Imprisoned Ship on the Imprisoned Ships page. [fine] is a decimal string with two decimals (ADR-0008);
 * [savingsCoverFine] tells whether this Harbor's Savings cover it, so the client never compares money.
 */
data class ImprisonedShipResponse(
    val shipId: UUID,
    val name: String,
    val catain: String,
    val homeHarbor: String,
    val fine: String,
    val savingsCoverFine: Boolean,
)
