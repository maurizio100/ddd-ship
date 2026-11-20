package com.sonicdevelopment.domain.ports.driving.ship

import com.sonicdevelopment.domain.model.values.RecruitId
import com.sonicdevelopment.domain.model.values.ShipId

interface CrewManagementPort {
    /** Hires the Recruit onto the ship, paying the Hiring Fee; null if the ship is not in this Harbor's fleet. */
    fun hire(shipId: ShipId, recruitId: RecruitId): HireDTO?
}
