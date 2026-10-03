package com.sonicdevelopment.driving.adapter.messaging.events

/** Inbound copy of another Harbor's `harbor-opened` payload. */
data class HarborOpenedInboundEvent(val harborName: String)
