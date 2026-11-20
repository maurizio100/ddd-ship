package com.sonicdevelopment.driving.adapter.web.mapper

import com.sonicdevelopment.domain.ports.driving.ship.ShipDTO
import com.sonicdevelopment.domain.ports.driving.ship.ShipDetailDTO
import com.sonicdevelopment.driving.adapter.web.responsemodel.CargoResponse
import com.sonicdevelopment.driving.adapter.web.responsemodel.CrewMemberResponse
import com.sonicdevelopment.driving.adapter.web.responsemodel.ShipDetailResponse
import com.sonicdevelopment.driving.adapter.web.responsemodel.ShipOverviewResponse
import com.sonicdevelopment.driving.adapter.web.responsemodel.toResponse

fun toShipResponse(ship: ShipDTO) =
    ShipOverviewResponse(
        id = ship.id.id,
        name = ship.name,
        catain = ship.catain,
        shippingState = ship.shippingState,
        arrivedFrom = ship.arrivedFrom,
        incoming = ship.incoming,
        homeHarbor = ship.homeHarbor,
        earnings = ship.earnings.toDecimalString(),
        imprisoned = ship.imprisoned,
        shipClass = ship.shipClass,
        crewCapacity = ship.shipClass.crewCapacity,
        crew = ship.crew.map { CrewMemberResponse(it.id, it.name, it.trait, it.portrait.toResponse()) },
    )
fun toShipDetailResponse(ship: ShipDetailDTO) =
    ShipDetailResponse(
        id = ship.id.id,
        name = ship.name,
        cargo = ship.cargo.map {
            CargoResponse(
                id = it.id.id,
                name = it.name,
                weight = it.weight
            )
        },
        cargoAboard = ship.cargoAboard.map {
            CargoResponse(
                id = it.id.id,
                name = it.name,
                weight = it.weight
            )
        },
        weight = ship.actualWeight,
        maxweight = ship.maxWeight,
        arrivedFrom = ship.arrivedFrom,
        homeHarbor = ship.homeHarbor,
    )