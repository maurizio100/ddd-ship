package com.sonicdevelopment.domain.exception

/** A ship cannot start a new Shipping: it is an Incoming Ship, or it already has an Active Shipping. */
class NewShippingRefusedException(message: String) : RuntimeException(message)
