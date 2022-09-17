package at.willhaben.application

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@ConfigurationPropertiesScan("at.willhaben")
@EnableJpaRepositories("at.willhaben.driven.adapter")
@EntityScan("at.willhaben.driven.adapter.output")
@SpringBootApplication(scanBasePackages = ["at.willhaben"])
open class ShipBackendApplication

fun main(args: Array<String>) {
    runApplication<ShipBackendApplication>(*args)
}
