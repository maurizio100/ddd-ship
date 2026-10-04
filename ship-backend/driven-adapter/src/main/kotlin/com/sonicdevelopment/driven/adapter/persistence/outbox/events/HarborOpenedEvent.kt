package com.sonicdevelopment.driven.adapter.persistence.outbox.events

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

/** Payload of `harbor-opened`: the Harbor Name of the Harbor that has just opened. */
data class HarborOpenedEvent @JsonCreator constructor(
    @JsonProperty("harborName") val harborName: String
)
