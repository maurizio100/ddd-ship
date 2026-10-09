package com.sonicdevelopment.domain.exception

/** A ship cannot be refused because it is at its own Home Harbor. */
class ShipAtItsHomeHarborException(message: String) : RuntimeException(message)
