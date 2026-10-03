package com.sonicdevelopment.driving.adapter.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Component
class InboundMessageReader(objectMapper: ObjectMapper) {

    fun read(record: ConsumerRecord<String, String>): InboundMessage = TODO()

    fun <T : Any> payloadAs(message: InboundMessage, type: KClass<T>): T = TODO()
}
