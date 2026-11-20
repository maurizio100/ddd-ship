package com.sonicdevelopment.domain.ports.driving.ship

import java.util.UUID

/** A Crew Member aboard a ship. */
data class CrewMemberDTO(val id: UUID, val name: String, val trait: String)
