package com.sonicdevelopment.domain.exception

/** A Recruit cannot be Hired: the ship is at sea or away from its Home Harbor, its Crew is full, or the Recruit is not offered. */
class DismissRefusedException(message: String) : RuntimeException(message)
