package com.sonicdevelopment.domain.model.values

import java.time.LocalDateTime

class SailorsCode(currentWeight: Float) {
    val codeValue = (currentWeight * LocalDateTime.now().minute).toInt().mod(14)
}
