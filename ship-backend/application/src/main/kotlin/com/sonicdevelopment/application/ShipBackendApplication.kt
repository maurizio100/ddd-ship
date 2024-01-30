package com.sonicdevelopment.application

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@ConfigurationPropertiesScan("com.sonicdevelopment")
@EnableJpaRepositories("com.sonicdevelopment.driven.adapter.persistence")
@EntityScan("com.sonicdevelopment.driven.adapter.persistence")
@SpringBootApplication(scanBasePackages = ["com.sonicdevelopment"])
class ShipBackendApplication

fun main(args: Array<String>) {
    runApplication<ShipBackendApplication>(*args)
}
