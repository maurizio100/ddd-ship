package com.sonicdevelopment.application.config

import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.ports.driving.harbor.HarborManagementPort
import com.sonicdevelopment.domain.service.PriceRoll
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import kotlin.random.Random

@Configuration
class HarborConfiguration {

    /** The Harbor this instance is (ADR-0003). */
    @Bean
    fun currentHarbor(harbor: HarborProperties): HarborName = HarborName(harbor.name)

    /** Rolls the Prices of this Harbor at its opening. Tests construct their own `PriceRoll` with a controlled `Random`. */
    @Bean
    fun priceRoll(): PriceRoll = PriceRoll(Random.Default)

    /**
     * Opens the Harbor once per startup (ADR-0005): it rolls the Prices the Harbor has not rolled yet and
     * writes the outbox row, so startup never waits for Kafka. A failed Price roll aborts startup, like a
     * failed outbox write: the Harbor does not open without its Prices.
     */
    @Bean
    fun openHarborOnStartup(harborManagementPort: HarborManagementPort) = ApplicationRunner {
        harborManagementPort.openHarbor()
    }
}
