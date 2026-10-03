package com.sonicdevelopment.driving.adapter.web.fleetevents

import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.ports.driven.FleetEventsPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

/**
 * Pushes fleet changes to the Users' browsers over Server-Sent Events (ADR-0006).
 *
 * Inside a transaction the push is deferred to `afterCommit`, so nothing is pushed on rollback; outside one
 * it is pushed at once. A failed push is logged and never thrown back to the caller, whose change has
 * already committed.
 */
@Component
class SseFleetEventsAdapter(
    private val fleetEventEmitters: FleetEventEmitters,
) : FleetEventsPort {

    override fun announceShipArrived(ship: Ship, originHarbor: HarborName) {
        afterCommit(SHIP_ARRIVED, ShipArrivedFleetEvent(ship.id.id, ship.shipName, originHarbor.name))
    }

    override fun announceShipLeft(ship: Ship, destinationHarbor: HarborName?) {
        afterCommit(SHIP_LEFT, ShipLeftFleetEvent(ship.id.id, ship.shipName, destinationHarbor?.name))
    }

    private fun afterCommit(eventName: String, data: Any) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            push(eventName, data)
            return
        }
        TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
            override fun afterCommit() = push(eventName, data)
        })
    }

    private fun push(eventName: String, data: Any) {
        try {
            fleetEventEmitters.broadcast(eventName, data)
        } catch (e: Exception) {
            log.warn("Could not push the fleet event {}", eventName, e)
        }
    }

    companion object {
        const val SHIP_ARRIVED = "ship-arrived"
        const val SHIP_LEFT = "ship-left"
        private val log = LoggerFactory.getLogger(SseFleetEventsAdapter::class.java)
    }
}
