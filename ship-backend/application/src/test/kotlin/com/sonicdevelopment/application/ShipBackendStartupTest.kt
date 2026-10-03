package com.sonicdevelopment.application

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.kafka.core.ConsumerFactory

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "harbor.name=Test Harbor",
        "spring.kafka.bootstrap-servers=localhost:1",
    ]
)
@Import(PostgresTestcontainer::class)
class ShipBackendStartupTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var consumerFactory: ConsumerFactory<*, *>

    @Autowired
    lateinit var flyway: Flyway

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @Test
    fun `starts and reports ready without a reachable Kafka`() {
        val response = restTemplate.getForEntity("/actuator/health/readiness", String::class.java)

        response.statusCode shouldBe HttpStatus.OK
    }

    @Test
    fun `uses the Harbor's consumer group`() {
        consumerFactory.configurationProperties[ConsumerConfig.GROUP_ID_CONFIG] shouldBe "ship-backend-Test Harbor"
    }

    @Test
    fun `migrates the inbox table`() {
        flyway.info().applied().map { it.version.version } shouldContain "5"
        jdbcTemplate.queryForObject("SELECT to_regclass('public.inbox_events') IS NOT NULL", Boolean::class.java) shouldBe true
    }
}
