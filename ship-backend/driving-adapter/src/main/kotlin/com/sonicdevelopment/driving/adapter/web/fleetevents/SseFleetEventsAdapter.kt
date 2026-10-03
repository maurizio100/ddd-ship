package com.sonicdevelopment.driving.adapter.web.fleetevents

import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.ports.driven.FleetEventsPort
import org.springframework.stereotype.Component

@Component
class SseFleetEventsAdapter(
    private val fleetEventEmitters: FleetEventEmitters,
) : FleetEventsPort {

    override fun announceShipArrived(ship: Ship, originHarbor: HarborName): Unit = TODO()

    override fun announceShipLeft(ship: Ship, destinationHarbor: HarborName?): Unit = TODO()
}
