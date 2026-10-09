package com.sonicdevelopment.application

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@ConfigurationPropertiesScan("com.sonicdevelopment")
@SpringBootApplication(scanBasePackages = ["com.sonicdevelopment"])
class ShipBackendApplication

fun main(args: Array<String>) {
    runApplication<ShipBackendApplication>(*args)
}
