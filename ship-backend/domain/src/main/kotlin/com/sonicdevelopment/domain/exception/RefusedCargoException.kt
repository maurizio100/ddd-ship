package com.sonicdevelopment.domain.exception

/** Cargo aboard that the Home Harbor refused cannot be unloaded into its Stock; it can only be delivered elsewhere. */
class RefusedCargoException(message: String) : RuntimeException(message)
