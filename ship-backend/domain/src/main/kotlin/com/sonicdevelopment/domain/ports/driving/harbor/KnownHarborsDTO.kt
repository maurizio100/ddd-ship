package com.sonicdevelopment.domain.ports.driving.harbor

import com.sonicdevelopment.domain.model.values.HarborName

/** This Harbor's own Harbor Name and the Known Harbors it has learned of. */
data class KnownHarborsDTO(
    val harborName: HarborName,
    val knownHarbors: List<HarborName>
)
