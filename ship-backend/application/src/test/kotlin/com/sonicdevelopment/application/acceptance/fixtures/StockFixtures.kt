package com.sonicdevelopment.application.acceptance.fixtures

import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import java.util.*

/** The Starting Stock seeded by `V7__stocks.sql`: this many of every Cargo. */
const val STARTING_STOCK = 3

/** The Stock of this Harbor holds [quantity] of the Cargo [cargoName]. */
fun FakeDrivenPorts.givenStockOf(cargoName: String, quantity: Int) {
    stock.setQuantity(SeedData.cargoIdOf(cargoName), quantity)
}

/** A ship commanded by a seeded Catain with a new Shipping, i.e. being prepared. Returns its id. */
fun TestRestTemplate.aShipBeingPrepared(name: String = "Black Pearl"): UUID {
    val ship = postForEntity("/web/ships", mapOf("name" to name, "catainId" to SeedData.aCatainId), Map::class.java)
    check(ship.statusCode == HttpStatus.OK) { "Creating the ship answered ${ship.statusCode}" }
    val shipId = UUID.fromString(ship.body!!["id"] as String)

    val shipping = postForEntity("/web/ships/$shipId/shippings", null, Map::class.java)
    check(shipping.statusCode == HttpStatus.OK) { "Creating the Shipping answered ${shipping.statusCode}" }
    return shipId
}
