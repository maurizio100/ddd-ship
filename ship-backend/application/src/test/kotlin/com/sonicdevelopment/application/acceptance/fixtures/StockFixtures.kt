package com.sonicdevelopment.application.acceptance.fixtures

import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import java.util.*

/** The Starting Stock seeded by `V7__stocks.sql`: this many of every Cargo. */
const val STARTING_STOCK = 3

/** Truncates what the scenarios change; the reference tables (`cargos`, `catains`, `quotes`) stay. */
fun JdbcTemplate.truncateMutableTables() {
    execute("TRUNCATE TABLE ships_cargos, shippings, ships, shipping_outbox, inbox_events, known_harbors")
}

/** Puts the Stock back to the Starting Stock: [STARTING_STOCK] of every catalog Cargo. */
fun JdbcTemplate.resetStockToStartingStock() {
    execute("DELETE FROM stocks")
    execute(
        "INSERT INTO stocks (id, cargo_id, stock_quantity) SELECT nextval('stocks_seq'), id, $STARTING_STOCK FROM cargos"
    )
}

/** The Stock of this Harbor holds [quantity] of the Cargo [cargoName]. */
fun JdbcTemplate.givenStockOf(cargoName: String, quantity: Int) {
    val updated = update(
        "UPDATE stocks SET stock_quantity = ? WHERE cargo_id = (SELECT id FROM cargos WHERE cargo_name = ?)",
        quantity, cargoName
    )
    check(updated == 1) { "No Stock entry for $cargoName" }
}

/** The business id of the seeded Cargo [cargoName]. */
fun JdbcTemplate.cargoIdOf(cargoName: String): UUID =
    queryForObject("SELECT cargo_id FROM cargos WHERE cargo_name = ?", UUID::class.java, cargoName)!!

/** A ship commanded by a seeded Catain with a new Shipping, i.e. being prepared. Returns its id. */
fun TestRestTemplate.aShipBeingPrepared(jdbcTemplate: JdbcTemplate, name: String = "Black Pearl"): UUID {
    val catainId = jdbcTemplate.queryForObject("SELECT catain_id FROM catains ORDER BY id LIMIT 1", UUID::class.java)!!

    val ship = postForEntity("/web/ships", mapOf("name" to name, "catainId" to catainId), Map::class.java)
    check(ship.statusCode == HttpStatus.OK) { "Creating the ship answered ${ship.statusCode}" }
    val shipId = UUID.fromString(ship.body!!["id"] as String)

    val shipping = postForEntity("/web/ships/$shipId/shippings", null, Map::class.java)
    check(shipping.statusCode == HttpStatus.OK) { "Creating the Shipping answered ${shipping.statusCode}" }
    return shipId
}
