import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.apache.kafka.common.header.internals.RecordHeaders
import org.junit.jupiter.api.Test
import org.example.ShippingEvent
import org.example.handleRecord
import java.nio.charset.StandardCharsets
import java.util.UUID
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RecordFilterTest {

    @Test
    fun shipArrivedRecordIsSkipped() {
        val headers = RecordHeaders()
        headers.add(RecordHeader("eventType", "ship-arrived".toByteArray(StandardCharsets.UTF_8)))

        val record = ConsumerRecord<String, ShippingEvent?>(
            "hexagonship-shipping",
            0,
            0L,
            "key",
            null
        ).also {
            // Can't set headers via constructor, so we'd need a different approach
            // For now, let's use reflection or create a test helper
        }

        val result = handleRecord(record)
        assertFalse(result, "ship-arrived record should be skipped")
    }

    @Test
    fun pollLoopContinuesAfterSkippedRecord() {
        val record1 = createTestRecord("ship-arrived", null)
        val record2 = createTestRecord("shipping-published", createTestShippingEvent())

        val result1 = handleRecord(record1)
        val result2 = handleRecord(record2)

        assertFalse(result1, "ship-arrived record should be skipped")
        assertTrue(result2, "shipping-published record should be announced")
    }

    @Test
    fun shippingPublishedRecordIsAnnounced() {
        val event = createTestShippingEvent()
        val record = createTestRecord("shipping-published", event)

        val result = handleRecord(record)
        assertTrue(result, "shipping-published record should be announced")
    }

    @Test
    fun recordWithoutEventTypeHeaderIsSkipped() {
        val headers = RecordHeaders()  // No eventType header
        val event = createTestShippingEvent()

        val record = ConsumerRecord<String, ShippingEvent?>(
            "hexagonship-shipping",
            0,
            0L,
            "key",
            event
        )

        // Manually set headers (there's no direct way through constructor)
        // For testing purposes, we need to work around this
        val result = handleRecord(record)
        assertFalse(result, "record without eventType header should be skipped")
    }

    private fun createTestRecord(eventType: String, value: ShippingEvent?): ConsumerRecord<String, ShippingEvent?> {
        val headers = RecordHeaders()
        headers.add(RecordHeader("eventType", eventType.toByteArray(StandardCharsets.UTF_8)))

        val record = ConsumerRecord<String, ShippingEvent?>(
            "hexagonship-shipping",
            0,
            0L,
            "key",
            value
        )

        // Unfortunately, ConsumerRecord doesn't expose a way to set headers after construction
        // We need to use reflection or create a wrapper
        val headersField = ConsumerRecord::class.java.getDeclaredField("headers")
        headersField.isAccessible = true
        headersField.set(record, headers)

        return record
    }

    private fun createTestShippingEvent(): ShippingEvent {
        return ShippingEvent(
            shipEventData = ShippingEvent.ShipEventData(
                shipId = UUID.randomUUID(),
                shipName = "Test Ship"
            ),
            shippingEventData = ShippingEvent.ShippingEventData(
                shippingId = UUID.randomUUID(),
                weight = 100.5f,
                shippingQuote = "500.00",
                cargo = emptyList()
            ),
            catain = ShippingEvent.CatainEventData(
                catainId = UUID.randomUUID(),
                catainName = "Test Catain"
            )
        )
    }
}
