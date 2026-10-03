package com.sonicdevelopment.domain.exception

/** A Cargo cannot be loaded because the Harbor's Stock holds none of it. */
class CargoOutOfStockException(message: String) : RuntimeException(message)
