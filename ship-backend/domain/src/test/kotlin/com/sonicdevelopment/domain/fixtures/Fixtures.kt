package com.sonicdevelopment.domain.fixtures

import com.sonicdevelopment.domain.model.Cargo
import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.Shipping
import com.sonicdevelopment.domain.model.values.CargoId
import com.sonicdevelopment.domain.model.values.CatainId
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.model.values.ShippingId
import java.util.*

fun aCargo(
    name: String = "Rum",
    weight: Float = 5.5F,
    id: CargoId = CargoId(UUID.randomUUID()),
) = Cargo(id = id, name = name, weight = weight)

fun aShipping(id: ShippingId = ShippingId(UUID.randomUUID())) = Shipping(id = id)

/** A ship being prepared (with an active Shipping), loaded with [loadedCargo]. */
fun aShip(
    name: String = "Black Pearl",
    loadedCargo: List<Cargo> = emptyList(),
    activeShipping: Shipping? = aShipping(),
    id: ShipId = ShipId(UUID.randomUUID()),
    homeHarbor: HarborName = HarborName("Port Royal"),
) = Ship(
    id = id,
    name = name,
    catainId = CatainId(UUID.randomUUID()),
    catainName = "Furry Jones",
    homeHarbor = homeHarbor,
    activeShipping = activeShipping,
    cargoLoad = loadedCargo.toMutableList(),
)
