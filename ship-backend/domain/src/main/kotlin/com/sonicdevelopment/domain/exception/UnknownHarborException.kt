package com.sonicdevelopment.domain.exception

/** A ship cannot be Released to a Harbor that is not one of the Known Harbors. */
class UnknownHarborException(message: String) : RuntimeException(message)
