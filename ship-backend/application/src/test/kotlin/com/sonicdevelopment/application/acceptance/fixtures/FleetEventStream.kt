package com.sonicdevelopment.application.acceptance.fixtures

import com.fasterxml.jackson.databind.ObjectMapper
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.stream.Stream

/** One fleet event as a browser's EventSource sees it: its name and its JSON data. */
data class FleetEvent(val name: String, val data: Map<*, *>)

/**
 * A User looking at the fleet: an open `GET /web/fleet-events` stream, read line by line on its own thread,
 * as a browser's EventSource would. It is open once the `:connected` comment has come through.
 */
class FleetEventStream private constructor(
    private val client: HttpClient,
    private val lines: Stream<String>,
) : AutoCloseable {

    private val events = LinkedBlockingQueue<FleetEvent>()
    private val connected = LinkedBlockingQueue<Unit>()

    private val reader = Thread {
        var name: String? = null
        var data: String? = null
        try {
            lines.forEach { line ->
                when {
                    line.startsWith(":connected") -> connected.put(Unit)
                    line.startsWith("event:") -> name = line.removePrefix("event:").trim()
                    line.startsWith("data:") -> data = line.removePrefix("data:").trim()
                    line.isEmpty() && name != null -> {
                        events.put(FleetEvent(name!!, ObjectMapper().readValue(data ?: "{}", Map::class.java)))
                        name = null
                        data = null
                    }
                }
            }
        } catch (_: Exception) {
            // the stream was closed
        }
    }.apply { isDaemon = true; start() }

    private fun awaitConnected() {
        checkNotNull(connected.poll(10, TimeUnit.SECONDS)) { "The fleet-events stream did not open" }
    }

    /** The next fleet event, or `null` if none comes within [timeout]. */
    fun next(timeout: Duration = Duration.ofSeconds(30)): FleetEvent? =
        events.poll(timeout.toMillis(), TimeUnit.MILLISECONDS)

    override fun close() {
        lines.close()
        client.shutdownNow()
    }

    companion object {
        /** Opens the fleet-events stream of the Harbor at [baseUrl] and waits until it is connected. */
        fun open(baseUrl: String): FleetEventStream {
            val client = HttpClient.newHttpClient()
            val response = client.send(
                HttpRequest.newBuilder(URI.create("$baseUrl/web/fleet-events"))
                    .header("Accept", "text/event-stream")
                    .GET()
                    .build(),
                HttpResponse.BodyHandlers.ofLines(),
            )
            check(response.statusCode() == 200) { "GET /web/fleet-events answered ${response.statusCode()}" }
            return FleetEventStream(client, response.body()).also { it.awaitConnected() }
        }
    }
}
