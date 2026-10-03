package com.sonicdevelopment.application.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("harbor")
data class HarborProperties(val name: String) {
    val consumerGroupId: String
        get() = TODO()
}
