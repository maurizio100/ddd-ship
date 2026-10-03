package com.sonicdevelopment.driving.adapter.web.fleetevents

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList

/**
 * An [SseEmitter] that records what is sent to it instead of writing to an HTTP response, and fails every
 * send once [failing] is set, like a browser tab that has gone away. Lifecycle callbacks are kept, so a test
 * can play the servlet container's part and fire them.
 */
class RecordingSseEmitter : SseEmitter() {

    @Volatile
    var failing = false

    /** Every sent event, rendered as SSE text (`event:…`, `data:…`, `:comment`). */
    val sent = CopyOnWriteArrayList<String>()

    val sentData = CopyOnWriteArrayList<Any>()

    var completionCallback: Runnable? = null
    var timeoutCallback: Runnable? = null
    var errorCallback: ((Throwable) -> Unit)? = null

    override fun send(builder: SseEventBuilder) {
        if (failing) throw IOException("Broken pipe")
        val parts = builder.build()
        sent += parts.joinToString("") { it.data.toString() }
        parts.map { it.data }.filter { it !is String }.forEach { sentData += it }
    }

    override fun onCompletion(callback: Runnable) {
        completionCallback = callback
    }

    override fun onTimeout(callback: Runnable) {
        timeoutCallback = callback
    }

    override fun onError(callback: java.util.function.Consumer<Throwable>) {
        errorCallback = { callback.accept(it) }
    }
}
