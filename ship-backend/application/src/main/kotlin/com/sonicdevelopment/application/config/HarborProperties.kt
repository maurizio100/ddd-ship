package com.sonicdevelopment.application.config

import org.springframework.boot.context.properties.ConfigurationProperties

/** This backend instance's identity as a Harbor (ADR-0003). */
@ConfigurationProperties("harbor")
data class HarborProperties(val name: String) {

    init {
        require(name.isNotBlank()) { "harbor.name (HARBOR_NAME) must be set: every ship-backend is one Harbor" }
    }

    /** One consumer group per Harbor, so replicas of a Harbor share the work and each Harbor sees every event. */
    val consumerGroupId: String
        get() = "ship-backend-$name"
}
