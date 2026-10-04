package com.sonicdevelopment.application.config

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Configuration

class HarborPropertiesTest {

    @Test
    fun `derives the consumer group from the Harbor Name`() {
        HarborProperties("Port A").consumerGroupId shouldBe "ship-backend-Port A"
    }

    @Test
    fun `fails to start without a Harbor Name`() {
        ApplicationContextRunner()
            .withUserConfiguration(HarborPropertiesConfiguration::class.java)
            .withPropertyValues("harbor.name=")
            .run { context ->
                val failure = context.startupFailure
                failure.shouldNotBeNull()
                generateSequence(failure as Throwable) { it.cause }.last().message shouldContain "harbor.name"
            }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(HarborProperties::class)
    class HarborPropertiesConfiguration
}
