package com.sonicdevelopment.application.acceptance.fixtures

import org.springframework.jdbc.core.JdbcTemplate

/**
 * The `JdbcTemplate` fixtures of the `-Pdb` tests that run against the real database.
 */

/** Truncates what the scenarios change; the reference tables (`cargos`, `catains`, `quotes`) stay. */
fun JdbcTemplate.truncateMutableTables() {
    execute("TRUNCATE TABLE ships_cargos, shippings, ships, shipping_outbox, inbox_events, known_harbors, arrivals")
}

/** Puts the Stock back to the Starting Stock: [STARTING_STOCK] of every catalog Cargo. */
fun JdbcTemplate.resetStockToStartingStock() {
    execute("DELETE FROM stocks")
    execute(
        "INSERT INTO stocks (id, cargo_id, stock_quantity) SELECT nextval('stocks_seq'), id, $STARTING_STOCK FROM cargos"
    )
}

/** Puts the Savings back to the Starting Savings, [STARTING_SAVINGS]. */
fun JdbcTemplate.resetSavingsToStartingSavings() {
    update("UPDATE savings SET savings_amount = ?::numeric", STARTING_SAVINGS)
}

/** Makes [names] Known Harbors of this Harbor, as the Harbor Opening would. */
fun JdbcTemplate.givenKnownHarbors(vararg names: String) {
    names.forEach {
        update("INSERT INTO known_harbors (id, harbor_name) VALUES (nextval('known_harbors_seq'), ?)", it)
    }
}
