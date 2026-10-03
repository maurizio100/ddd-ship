import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.apache.kafka.common.header.internals.RecordHeaders
import org.apache.kafka.common.record.TimestampType
import org.example.ShippingEvent
import org.example.ShippingEventDeserializer
import org.example.handleRecord
import org.junit.jupiter.api.Test
import java.util.Optional
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RecordFilterTest {

    private val deserializer = ShippingEventDeserializer()

    @Test
    fun shipArrivedRecordIsSkipped() {
        // Non-null value, so only the eventType header can cause the skip.
        val record = record("ship-arrived", publishedEvent())

        assertFalse(handleRecord(record), "ship-arrived record should be skipped")
    }

    @Test
    fun shipArrivedSamplePayloadIsSkipped() {
        val record = record("ship-arrived", deserializer.deserialize("t", sample("ship-arrived")))

        assertFalse(handleRecord(record), "ship-arrived record should be skipped")
    }

    @Test
    fun pollLoopContinuesAfterSkippedRecord() {
        val skipped = handleRecord(record("ship-arrived", publishedEvent(), offset = 0L))
        val announced = handleRecord(record("shipping-published", publishedEvent(), offset = 1L))

        assertFalse(skipped, "ship-arrived record should be skipped")
        assertTrue(announced, "shipping-published record after a skipped one should be announced")
    }

    @Test
    fun shippingPublishedRecordIsAnnounced() {
        assertTrue(handleRecord(record("shipping-published", publishedEvent())))
    }

    @Test
    fun recordWithoutEventTypeHeaderIsSkipped() {
        val record = ConsumerRecord<String, ShippingEvent?>(
            "hexagonship-shipping", 0, 0L, 0L, TimestampType.NO_TIMESTAMP_TYPE, 0, 0,
            "key", publishedEvent(), RecordHeaders(), Optional.empty()
        )

        assertFalse(handleRecord(record), "record without eventType header should be skipped")
    }

    @Test
    fun shippingPublishedRecordWithoutValueIsSkipped() {
        assertFalse(handleRecord(record("shipping-published", null)))
    }

    private fun sample(name: String): ByteArray =
        javaClass.getResourceAsStream("/events/$name.json")!!.readBytes()

    private fun publishedEvent(): ShippingEvent {
        val event = deserializer.deserialize("t", sample("shipping-published"))
        assertNotNull(event, "shipping-published sample must parse")
        return event
    }

    private fun record(eventType: String, value: ShippingEvent?, offset: Long = 0L) =
        ConsumerRecord<String, ShippingEvent?>(
            "hexagonship-shipping", 0, offset, 0L, TimestampType.NO_TIMESTAMP_TYPE, 0, 0,
            "key", value,
            RecordHeaders().add(RecordHeader("eventType", eventType.toByteArray(Charsets.UTF_8))),
            Optional.empty()
        )
}
