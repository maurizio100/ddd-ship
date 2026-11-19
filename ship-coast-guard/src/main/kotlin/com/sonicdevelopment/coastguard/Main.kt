package com.sonicdevelopment.coastguard

import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsConfig
import java.util.Properties

fun main() {
    val props = Properties().apply {
        put(StreamsConfig.APPLICATION_ID_CONFIG, "ship-coast-guard")
        put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, System.getenv("KAFKA_BOOTSTRAP_SERVERS") ?: "localhost:9094")
        put("auto.offset.reset", "earliest")
    }
    val streams = KafkaStreams(CoastGuardTopology.build(), props)
    Runtime.getRuntime().addShutdownHook(Thread { streams.close() })
    streams.start()
}
