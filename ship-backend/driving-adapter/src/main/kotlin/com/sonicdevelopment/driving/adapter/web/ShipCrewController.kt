package com.sonicdevelopment.driving.adapter.web

import com.sonicdevelopment.domain.model.values.CrewMemberId
import com.sonicdevelopment.domain.model.values.RecruitId
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.ports.driving.ship.CrewManagementPort
import com.sonicdevelopment.driving.adapter.web.mapper.toHireResponse
import com.sonicdevelopment.driving.adapter.web.requestmodel.HireRequest
import com.sonicdevelopment.driving.adapter.web.responsemodel.HireResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

/** The Crew of a ship: Hiring a Recruit onto it and Dismissing a Crew Member from it. Nothing is published. */
@RestController
@CrossOrigin(origins = [])
@RequestMapping("/web/ships/{shipId}/crew")
class ShipCrewController(private val crewManagementPort: CrewManagementPort) {

    /** Pays the Hiring Fee and Hires the Recruit; answers with the Hire. The client reads the fleet, Savings and pool back from their GETs. */
    @PostMapping
    fun hire(@PathVariable shipId: UUID, @RequestBody request: HireRequest): HireResponse =
        crewManagementPort.hire(ShipId(shipId), RecruitId(request.recruitId))?.let { toHireResponse(it) }
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource")

    /** Dismisses the Crew Member: no refund, and they never return to the Recruit Pool. Answers 204. */
    @DeleteMapping("/{crewMemberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun dismiss(@PathVariable shipId: UUID, @PathVariable crewMemberId: UUID) {
        if (!crewManagementPort.dismiss(ShipId(shipId), CrewMemberId(crewMemberId))) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource")
        }
    }
}
