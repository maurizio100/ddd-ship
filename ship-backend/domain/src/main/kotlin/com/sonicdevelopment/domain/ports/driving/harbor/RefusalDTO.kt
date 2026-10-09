package com.sonicdevelopment.domain.ports.driving.harbor

import com.sonicdevelopment.domain.model.values.HarborName

/** The outcome of refusing an Incoming Ship: [sailsTo] is the Home Harbor it sails to, `null` when it stays in this fleet. */
data class RefusalDTO(val sailsTo: HarborName?)
