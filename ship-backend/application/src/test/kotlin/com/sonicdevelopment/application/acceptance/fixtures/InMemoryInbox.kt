package com.sonicdevelopment.application.acceptance.fixtures

import com.sonicdevelopment.domain.model.values.EventId
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.model.values.ShippingId
import com.sonicdevelopment.domain.ports.driven.ArrivalRepositoryPort
import com.sonicdevelopment.domain.ports.driven.InboxRepositoryPort
import com.sonicdevelopment.domain.ports.driven.KnownHarborRepositoryPort
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/** First write wins, like the inbox's `ON CONFLICT DO NOTHING`. */
class InMemoryInbox : InboxRepositoryPort {
    private val consumed = ConcurrentHashMap.newKeySet<EventId>()

    override fun recordConsumedEvent(eventId: EventId): Boolean = consumed.add(eventId)

    fun hasConsumed(eventId: UUID): Boolean = consumed.contains(EventId(eventId))

    fun reset() = consumed.clear()
}

/** First write per Shipping wins. */
class InMemoryArrivals : ArrivalRepositoryPort {
    private val arrivals = ConcurrentHashMap<ShippingId, ShipId>()

    override fun recordArrival(shippingId: ShippingId, shipId: ShipId): Boolean =
        arrivals.putIfAbsent(shippingId, shipId) == null

    fun reset() = arrivals.clear()
}

/** One entry per Harbor Name, listed sorted by name. */
class InMemoryKnownHarbors : KnownHarborRepositoryPort {
    private val harbors = ConcurrentHashMap.newKeySet<HarborName>()

    override fun rememberHarbor(harborName: HarborName) {
        harbors.add(harborName)
    }

    override fun getKnownHarbors(): List<HarborName> = harbors.sortedBy { it.name }

    fun reset() = harbors.clear()
}
