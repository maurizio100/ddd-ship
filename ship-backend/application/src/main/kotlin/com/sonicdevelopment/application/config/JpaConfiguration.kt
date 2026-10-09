package com.sonicdevelopment.application.config

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

/**
 * Kept off the main class so a test context can boot without JPA (ADR-0010).
 */
@Configuration
@EnableJpaRepositories("com.sonicdevelopment.driven.adapter.persistence")
@EntityScan("com.sonicdevelopment.driven.adapter.persistence")
class JpaConfiguration
