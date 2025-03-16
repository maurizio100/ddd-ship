package com.sonicdevelopment.driving.adapter.web.mapper

import com.sonicdevelopment.domain.ports.driving.ship.ShipDTO
import com.sonicdevelopment.domain.ports.driving.ship.ShipDetailDTO
import com.sonicdevelopment.driving.adapter.web.responsemodel.CargoResponse
import com.sonicdevelopment.driving.adapter.web.responsemodel.ShipDetailResponse
import com.sonicdevelopment.driving.adapter.web.responsemodel.ShipOverviewResponse

fun toShipResponse(ship: ShipDTO) =
    ShipOverviewResponse(
        id = ship.id.id,
        name = ship.name,
        shippingState = ship.shippingState
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
        weight = ship.actualWeight,
        maxweight = ship.maxWeight
    )