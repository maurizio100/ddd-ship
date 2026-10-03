package com.sonicdevelopment.application.config

import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.ports.driving.harbor.HarborManagementPort
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HarborConfiguration {

    /** The Harbor this instance is (ADR-0003). */
    @Bean
    fun currentHarbor(harbor: HarborProperties): HarborName = HarborName(harbor.name)

    /**
     * Opens the Harbor once per startup (ADR-0005). It only writes the outbox row, so startup never
     * waits for Kafka.
     */
    @Bean
    fun openHarborOnStartup(harborManagementPort: HarborManagementPort) = ApplicationRunner {
        harborManagementPort.openHarbor()
    }
}
