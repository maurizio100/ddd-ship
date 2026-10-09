package com.sonicdevelopment.application.acceptance.fixtures

/** The Starting Savings seeded by `V15__prices_and_savings.sql`. */
const val STARTING_SAVINGS = "1000.00"

/** The Price of the Cargo [cargoName] at this Harbor is [decimal], e.g. "42.00". */
fun FakeDrivenPorts.givenPriceOf(cargoName: String, decimal: String) {
    prices.setPrice(com.sonicdevelopment.domain.model.values.CargoId(SeedData.cargoIdOf(cargoName)), decimal)
}

/** The Savings of this Harbor are [decimal], e.g. "640.50". */
fun FakeDrivenPorts.givenSavings(decimal: String) {
    savings.setSavings(decimal)
}
