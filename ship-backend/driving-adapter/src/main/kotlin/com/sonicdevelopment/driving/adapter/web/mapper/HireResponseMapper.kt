package com.sonicdevelopment.driving.adapter.web.mapper

import com.sonicdevelopment.domain.ports.driving.ship.HireDTO
import com.sonicdevelopment.driving.adapter.web.responsemodel.CrewMemberResponse
import com.sonicdevelopment.driving.adapter.web.responsemodel.HireResponse
import com.sonicdevelopment.driving.adapter.web.responsemodel.RecruitResponse
import com.sonicdevelopment.driving.adapter.web.responsemodel.toResponse

fun toHireResponse(hire: HireDTO) =
    HireResponse(
        shipId = hire.shipId.id,
        name = hire.shipName,
        crewMember = CrewMemberResponse(hire.crewMember.id, hire.crewMember.name, hire.crewMember.trait, hire.crewMember.portrait.toResponse()),
        hiringFee = hire.hiringFee.toDecimalString(),
        savings = hire.savings.toDecimalString(),
        newRecruit = RecruitResponse(
            hire.newRecruit.id, hire.newRecruit.name, hire.newRecruit.trait, hire.newRecruit.hiringFee.toDecimalString(),
            hire.newRecruit.portrait.toResponse(),
        ),
    )
