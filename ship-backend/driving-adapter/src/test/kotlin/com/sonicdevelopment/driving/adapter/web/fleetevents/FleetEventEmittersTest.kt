package com.sonicdevelopment.driving.adapter.web.fleetevents

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTimeoutPreemptively
import java.io.IOException
import java.time.Duration
import java.util.UUID
import java.util.concurrent.CountDownLatch

class FleetEventEmittersTest {

    private val emitters = FleetEventEmitters(heartbeatIntervalMillis = 60_000)

    private val blackPearl = ShipArrivedFleetEvent(UUID.randomUUID(), "Black Pearl", "Tortuga")

    @AfterEach
    fun shutDown() {
        emitters.shutdown()
    }

    @Test
    fun `a new subscriber is told how soon to reconnect and that it is connected`() {
        val tab = RecordingSseEmitter()

        emitters.register(tab)

        tab.sent.single() shouldContain "retry:${FleetEventEmitters.RECONNECT_MILLIS}"
        tab.sent.single() shouldContain ":connected"
        emitters.count() shouldBe 1
    }

    @Test
    fun `a broadcast reaches every subscriber`() {
        val firstTab = RecordingSseEmitter()
        val secondTab = RecordingSseEmitter()
        emitters.register(firstTab)
        emitters.register(secondTab)

        emitters.broadcast("ship-arrived", blackPearl)

        eventually { firstTab.sentData shouldContainExactly listOf(blackPearl) }
        eventually { secondTab.sentData shouldContainExactly listOf(blackPearl) }
        firstTab.sent.last() shouldContain "event:ship-arrived"
    }

    @Test
    fun `a subscriber that cannot be sent to is dropped and the others still receive`() {
        val goneTab = RecordingSseEmitter()
        val openTab = RecordingSseEmitter()
        emitters.register(goneTab)
        emitters.register(openTab)
        goneTab.failing = true

        emitters.broadcast("ship-arrived", blackPearl)

        eventually { openTab.sentData shouldContainExactly listOf(blackPearl) }
        eventually { emitters.count() shouldBe 1 }

        emitters.broadcast("ship-arrived", blackPearl)
        eventually { openTab.sentData.size shouldBe 2 }
    }

    @Test
    fun `a subscriber that fails for another reason than a broken connection is dropped and completed`() {
        val brokenTab = RecordingSseEmitter().also { emitters.register(it) }
        brokenTab.failure = IllegalStateException("ResponseBodyEmitter has already completed")

        emitters.broadcast("ship-arrived", blackPearl)

        eventually { emitters.count() shouldBe 0 }
        brokenTab.completed shouldBe true
    }

    @Test
    fun `a broadcast does not wait for a subscriber that has stopped reading`() {
        val stalledTab = RecordingSseEmitter().also { emitters.register(it) }
        val openTab = RecordingSseEmitter().also { emitters.register(it) }
        val stall = CountDownLatch(1)
        stalledTab.gate = stall

        try {
            assertTimeoutPreemptively(Duration.ofSeconds(2)) {
                emitters.broadcast("ship-arrived", blackPearl)
                emitters.broadcast("ship-left", blackPearl)
            }
        } finally {
            stall.countDown()
        }

        eventually { stalledTab.sent.size shouldBe 3 }
        eventually { openTab.sent.size shouldBe 3 }
        openTab.sent.drop(1).map { it.substringBefore("\n") } shouldContainExactly
            listOf("event:ship-arrived", "event:ship-left")
    }

    @Test
    fun `a subscriber whose stream completes, times out or fails is dropped`() {
        val completed = RecordingSseEmitter().also { emitters.register(it) }
        val timedOut = RecordingSseEmitter().also { emitters.register(it) }
        val failed = RecordingSseEmitter().also { emitters.register(it) }
        emitters.count() shouldBe 3

        completed.completionCallback!!.run()
        timedOut.timeoutCallback!!.run()
        failed.errorCallback!!(IOException("Connection reset"))

        emitters.count() shouldBe 0
    }

    @Test
    fun `every subscriber gets a heartbeat`() {
        val heartbeating = FleetEventEmitters(heartbeatIntervalMillis = 50)
        try {
            val tab = RecordingSseEmitter()
            heartbeating.register(tab)

            await().atMost(Duration.ofSeconds(5)).untilAsserted {
                tab.sent.any { it.startsWith(":heartbeat\n") } shouldBe true
            }
        } finally {
            heartbeating.shutdown()
        }
    }

    @Test
    fun `shutting down completes every subscriber`() {
        RecordingSseEmitter().also { emitters.register(it) }

        emitters.shutdown()

        emitters.count() shouldBe 0
    }

    private fun eventually(assertion: () -> Unit) {
        await().atMost(Duration.ofSeconds(5)).untilAsserted(assertion)
    }
}
