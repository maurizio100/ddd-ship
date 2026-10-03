package com.sonicdevelopment.driving.adapter.web.fleetevents

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

/** The open fleet-event streams of this Harbor's Users: one emitter per subscribed browser tab. */
@Component
class FleetEventEmitters(
    @Value("\${fleet-events.heartbeat-interval-ms:15000}") private val heartbeatIntervalMillis: Long,
) {

    fun register(emitter: SseEmitter = SseEmitter(TIMEOUT_MILLIS)): SseEmitter = TODO()

    fun broadcast(eventName: String, data: Any): Unit = TODO()

    fun count(): Int = TODO()

    fun shutdown(): Unit = TODO()

    companion object {
        const val TIMEOUT_MILLIS = 30L * 60 * 1000
        const val RECONNECT_MILLIS = 3000L
    }
}
