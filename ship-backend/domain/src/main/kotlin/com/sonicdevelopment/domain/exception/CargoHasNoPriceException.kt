package com.sonicdevelopment.domain.exception

/** A Cargo cannot be bought because this Harbor has not rolled a Price for it yet. */
class CargoHasNoPriceException(message: String) : RuntimeException(message)
