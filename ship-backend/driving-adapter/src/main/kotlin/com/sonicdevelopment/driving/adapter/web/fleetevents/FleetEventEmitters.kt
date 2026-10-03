package com.sonicdevelopment.driving.adapter.web.fleetevents

import jakarta.annotation.PreDestroy
import java.io.IOException
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * The open fleet-event streams of this Harbor's Users: one emitter per subscribed browser tab, held in memory
 * by this instance only (arc42 R-11).
 *
 * A heartbeat comment keeps idle streams from being cut by proxies. An emitter is dropped when its stream
 * completes, times out or fails, or when a send to it fails; the browser's EventSource then reconnects by itself.
 *
 * Sends to the open streams are blocking servlet writes, so a broadcast and the heartbeat are handed to one
 * sender thread: they keep their order, and a browser that has stopped reading cannot stall the caller, which
 * is the Kafka listener thread that has just committed an Arrival.
 */
@Component
class FleetEventEmitters(
    @Value("\${fleet-events.heartbeat-interval-ms:15000}") heartbeatIntervalMillis: Long,
) {

    private val emitters = CopyOnWriteArraySet<SseEmitter>()

    private val sender: ExecutorService = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "fleet-events-sender").apply { isDaemon = true }
    }

    private val heartbeat: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor { runnable ->
        Thread(runnable, "fleet-events-heartbeat").apply { isDaemon = true }
    }.apply {
        scheduleAtFixedRate(
            { sendToAll { it.send(SseEmitter.event().comment("heartbeat")) } },
            heartbeatIntervalMillis, heartbeatIntervalMillis, TimeUnit.MILLISECONDS,
        )
    }

    /** Subscribes [emitter]: tells it how soon to reconnect and that it is connected. */
    fun register(emitter: SseEmitter = SseEmitter(TIMEOUT_MILLIS)): SseEmitter {
        emitter.onCompletion { emitters.remove(emitter) }
        emitter.onTimeout { emitters.remove(emitter) }
        emitter.onError { emitters.remove(emitter) }
        emitters.add(emitter)
        send(emitter) { it.send(SseEmitter.event().reconnectTime(RECONNECT_MILLIS).comment("connected")) }
        return emitter
    }

    /** Sends the event [eventName] with [data] as JSON to every subscriber, without waiting for the sends. */
    fun broadcast(eventName: String, data: Any) {
        sendToAll { it.send(SseEmitter.event().name(eventName).data(data, MediaType.APPLICATION_JSON)) }
    }

    fun count(): Int = emitters.size

    /**
     * Completes every stream as soon as the application starts closing, before the web server's graceful
     * shutdown, which would otherwise wait for the open streams until its timeout.
     */
    @EventListener(ContextClosedEvent::class)
    @PreDestroy
    fun shutdown() {
        heartbeat.shutdownNow()
        sender.shutdownNow()
        emitters.forEach { it.complete() }
        emitters.clear()
    }

    private fun sendToAll(action: (SseEmitter) -> Unit) {
        try {
            sender.execute { emitters.forEach { send(it, action) } }
        } catch (_: RejectedExecutionException) {
            // shutting down: there is nobody left to send to
        }
    }

    /** A tab that has gone away fails the send (`IOException`, or `IllegalStateException` once completed). */
    private fun send(emitter: SseEmitter, action: (SseEmitter) -> Unit) {
        try {
            action(emitter)
        } catch (e: Exception) {
            emitters.remove(emitter)
            if (e !is IOException) emitter.complete()
        }
    }

    companion object {
        const val TIMEOUT_MILLIS = 30L * 60 * 1000
        const val RECONNECT_MILLIS = 3000L
    }
}
