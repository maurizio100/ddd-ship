package com.sonicdevelopment.coastguard

import com.sonicdevelopment.coastguard.events.eventIdOf
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ShipWatchTest {
    private val minute = 60_000L

    @Test
    fun `a republished imprisonment carries the same event id`() {
        val a = UUID.randomUUID()
        val b = UUID.randomUUID()
        assertEquals(eventIdOf(a), eventIdOf(UUID.fromString(a.toString())))
        assertNotEquals(eventIdOf(a), eventIdOf(b))
    }

    @Test
    fun `a voyage exactly 10 minutes before still counts`() {
        val (w1, _) = ShipWatch().record(UUID.randomUUID(), 0)
        val (w2, i2) = w1.record(UUID.randomUUID(), 5 * minute)
        val (_, i3) = w2.record(UUID.randomUUID(), 10 * minute)
        assertFalse(i2)
        assertTrue(i3)
    }

    @Test
    fun `a voyage just over 10 minutes before does not count`() {
        val (w1, _) = ShipWatch().record(UUID.randomUUID(), 0)
        val (w2, _) = w1.record(UUID.randomUUID(), 5 * minute)
        val (_, i3) = w2.record(UUID.randomUUID(), 10 * minute + 1)
        assertFalse(i3)
    }

    @Test
    fun `a spent voyage redelivered after an imprisonment does not count again`() {
        val a = UUID.randomUUID()
        val (w1, _) = ShipWatch().record(a, 0)
        val (w2, _) = w1.record(UUID.randomUUID(), minute)
        val (w3, imprisoned) = w2.record(UUID.randomUUID(), 2 * minute)
        assertTrue(imprisoned)
        val (w4, i4) = w3.record(UUID.randomUUID(), 3 * minute)
        val (w5, i5) = w4.record(a, 4 * minute)
        val (_, i6) = w5.record(UUID.randomUUID(), 5 * minute)
        assertFalse(i4)
        assertFalse(i5)
        assertFalse(i6)
    }
}
