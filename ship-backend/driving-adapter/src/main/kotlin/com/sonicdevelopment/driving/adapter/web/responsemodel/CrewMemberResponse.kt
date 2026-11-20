package com.sonicdevelopment.driving.adapter.web.responsemodel

import java.util.UUID

/** A Crew Member aboard a ship. */
data class CrewMemberResponse(val id: UUID, val name: String, val trait: String, val portrait: PortraitResponse)
