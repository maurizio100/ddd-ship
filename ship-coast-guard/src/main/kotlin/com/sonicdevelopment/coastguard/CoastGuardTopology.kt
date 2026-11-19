package com.sonicdevelopment.coastguard

import org.apache.kafka.streams.Topology

const val SHIPPING_TOPIC = "hexagonship-shipping"
const val COAST_GUARD_TOPIC = "hexagonship-coast-guard"

object CoastGuardTopology {
    fun build(): Topology = Topology()
}
