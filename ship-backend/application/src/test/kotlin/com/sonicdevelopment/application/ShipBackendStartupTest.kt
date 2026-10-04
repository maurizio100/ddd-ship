package com.sonicdevelopment.application

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
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
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.core.ConsumerFactory
import java.util.*

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "harbor.name=Test Harbor",
        "spring.kafka.bootstrap-servers=localhost:1",
        "spring.kafka.listener.auto-startup=false",
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

    @Autowired
    lateinit var listenerRegistry: KafkaListenerEndpointRegistry

    @Test
    fun `starts and reports ready without a reachable Kafka`() {
        val response = restTemplate.getForEntity("/actuator/health/readiness", String::class.java)

        response.statusCode shouldBe HttpStatus.OK
        val harborListener = listenerRegistry.getListenerContainer("harbor-events")
        harborListener.shouldNotBeNull()
        harborListener.isRunning shouldBe false
    }

    @Test
    fun `writes Harbor Opened to the outbox on startup`() {
        val rows = jdbcTemplate.queryForList(
            "SELECT aggregate_type, event_type, payload FROM shipping_outbox WHERE aggregate_id = ?",
            UUID.fromString("8a5e4e90-5107-5422-a855-6d5c4f07d690")
        )

        rows shouldHaveSize 1
        rows.single()["aggregate_type"] shouldBe "harbor"
        rows.single()["event_type"] shouldBe "harbor-opened"
        rows.single()["payload"] shouldBe """{"harborName":"Test Harbor"}"""
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

    @Test
    fun `migrates the Known Harbors table`() {
        flyway.info().applied().map { it.version.version } shouldContain "6"
        jdbcTemplate.queryForObject("SELECT to_regclass('public.known_harbors') IS NOT NULL", Boolean::class.java) shouldBe true
    }
}
