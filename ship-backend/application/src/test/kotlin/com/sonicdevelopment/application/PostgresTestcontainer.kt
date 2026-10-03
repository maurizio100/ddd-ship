package com.sonicdevelopment.application

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

/** One PostgreSQL 13 container per JVM, shared by every test context that imports this configuration. */
@TestConfiguration(proxyBeanMethods = false)
class PostgresTestcontainer {

    @Bean
    @ServiceConnection
    fun postgresContainer(): PostgreSQLContainer<*> = container

    companion object {
        private val container: PostgreSQLContainer<*> = PostgreSQLContainer(DockerImageName.parse("postgres:13.6"))
    }
}
