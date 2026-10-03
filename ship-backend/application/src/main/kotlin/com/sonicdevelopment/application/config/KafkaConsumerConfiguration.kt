package com.sonicdevelopment.application.config

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaConsumerFactoryCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KafkaConsumerConfiguration {

    @Bean
    fun harborConsumerGroup(harbor: HarborProperties) = DefaultKafkaConsumerFactoryCustomizer {
        it.updateConfigs(mapOf(ConsumerConfig.GROUP_ID_CONFIG to harbor.consumerGroupId))
    }
}
