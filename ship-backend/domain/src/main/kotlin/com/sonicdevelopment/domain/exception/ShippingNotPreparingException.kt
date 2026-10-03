package com.sonicdevelopment.domain.exception

/** A ship can only be Released while its Shipping is being prepared. */
class ShippingNotPreparingException(message: String) : RuntimeException(message)
