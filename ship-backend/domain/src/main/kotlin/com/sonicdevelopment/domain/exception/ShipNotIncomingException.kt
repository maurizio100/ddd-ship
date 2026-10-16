package com.sonicdevelopment.domain.exception

/** A ship cannot be unloaded or refused because it is not an Incoming Ship (anymore). */
class ShipNotIncomingException(message: String) : RuntimeException(message)
