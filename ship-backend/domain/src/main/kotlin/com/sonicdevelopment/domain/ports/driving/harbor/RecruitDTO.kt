package com.sonicdevelopment.domain.ports.driving.harbor

import com.sonicdevelopment.domain.model.values.Money
import java.util.UUID

/** A Recruit offered in this Harbor's Recruit Pool. */
data class RecruitDTO(val id: UUID, val name: String, val trait: String, val hiringFee: Money)
