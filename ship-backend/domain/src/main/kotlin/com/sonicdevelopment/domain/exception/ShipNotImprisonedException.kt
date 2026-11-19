package com.sonicdevelopment.domain.exception

/** A Fine cannot be paid because the ship is not an Imprisoned Ship at this, its Home, Harbor (anymore). */
class ShipNotImprisonedException(message: String) : RuntimeException(message)
