package com.sonicdevelopment.domain.exception

import com.sonicdevelopment.domain.model.values.Money

/** A payment is refused because the Harbor's Savings hold less than its [cost]. */
class SavingsDoNotCoverException(
    val cost: Money,
    message: String = "The Savings do not cover $cost",
) : RuntimeException(message)
