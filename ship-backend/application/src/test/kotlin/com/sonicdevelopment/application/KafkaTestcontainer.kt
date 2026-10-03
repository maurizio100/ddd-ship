package com.sonicdevelopment.application

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.config.TopicConfig
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.kafka.KafkaContainer
import org.testcontainers.utility.DockerImageName

/**
 * One Kafka container per JVM, shared by every messaging test context that imports this configuration.
 * It creates the compacted `hexagonship-harbor` topic up front, as the topic script does for a real
 * Harbor network, so listeners never race topic auto-creation.
 */
@TestConfiguration(proxyBeanMethods = false)
class KafkaTestcontainer {

    @Bean
    @ServiceConnection
    fun kafkaContainer(): KafkaContainer = container

    companion object {
        const val HARBOR_TOPIC = "hexagonship-harbor"

        private val container: KafkaContainer by lazy {
            KafkaContainer(DockerImageName.parse("apache/kafka:3.9.1")).also {
                it.start()
                createHarborTopic(it.bootstrapServers)
            }
        }

        private fun createHarborTopic(bootstrapServers: String) {
            AdminClient.create(mapOf(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers)).use { admin ->
                val topic = NewTopic(HARBOR_TOPIC, 1, 1.toShort())
                    .configs(mapOf(TopicConfig.CLEANUP_POLICY_CONFIG to TopicConfig.CLEANUP_POLICY_COMPACT))
                admin.createTopics(listOf(topic)).all().get()
            }
        }
    }
}
