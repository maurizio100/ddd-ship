package com.sonicdevelopment.domain.exception

/** A Crew Member cannot be dismissed: the ship is at sea or away from its Home Harbor, or the Crew Member is not aboard. */
class DismissRefusedException(message: String) : RuntimeException(message)
