package com.sonicdevelopment.driving.adapter.web.responsemodel

import com.sonicdevelopment.domain.model.values.Portrait

/** A Recruit Portrait: lower-kebab names of the fur colour, coat pattern, eye colour and accessory. */
data class PortraitResponse(val fur: String, val coat: String, val eyes: String, val accessory: String)

fun Portrait.toResponse() = PortraitResponse(kebab(fur), kebab(coat), kebab(eyes), kebab(accessory))

private fun kebab(value: Enum<*>) = value.name.lowercase().replace('_', '-')
