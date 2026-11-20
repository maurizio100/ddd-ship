package com.sonicdevelopment.driving.adapter.web.mapper

import com.sonicdevelopment.domain.ports.driving.ship.DismissalDTO
import com.sonicdevelopment.driving.adapter.web.responsemodel.DismissalResponse

fun toDismissalResponse(dismissal: DismissalDTO) =
    DismissalResponse(
        shipId = dismissal.shipId.id,
        name = dismissal.shipName,
        crewMemberId = dismissal.crewMemberId.id,
        refund = dismissal.refund.toDecimalString(),
        savings = dismissal.savings.toDecimalString(),
    )
