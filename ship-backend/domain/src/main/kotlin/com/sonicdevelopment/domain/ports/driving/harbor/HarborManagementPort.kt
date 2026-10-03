package com.sonicdevelopment.domain.ports.driving.harbor

import com.sonicdevelopment.domain.model.values.EventId
import com.sonicdevelopment.domain.model.values.HarborName

interface HarborManagementPort {
    /** Announces this Harbor to all other Harbors by publishing Harbor Opened. */
    fun openHarbor()

    /** Handles the Harbor Opened event [eventId] of the Harbor [harborName]. */
    fun learnAboutHarbor(eventId: EventId, harborName: HarborName)
}
