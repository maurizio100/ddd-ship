import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.apache.kafka.common.header.internals.RecordHeaders
import org.apache.kafka.common.record.TimestampType
import org.example.handleRecord
import org.junit.jupiter.api.Test
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RecordFilterTest {

    @Test
    fun shippingPublishedIsAnnouncedAsDeparture() {
        val out = mutableListOf<String>()

        assertTrue(handleRecord(record("shipping-published", sample("shipping-published")), out::add))

        val text = out.joinToString("\n")
        assertTrue(text.contains("Titanic"), text)
        assertTrue(text.contains("Whiskers"), text)
        assertTrue(text.contains("100.5"), text)
        assertTrue(text.contains("Tea"), text)
    }

    @Test
    fun plainObjectValueIsParsedToo() {
        val out = mutableListOf<String>()
        val plain = """{"shipEventData":{"shipId":"550e8400-e29b-41d4-a716-446655440000","shipName":"Titanic"},"shippingEventData":{"shippingId":"550e8400-e29b-41d4-a716-446655440001","weight":100.5,"shippingQuote":null,"cargo":[]},"catain":{"catainId":"550e8400-e29b-41d4-a716-446655440003","catainName":"Whiskers"}}"""

        assertTrue(handleRecord(record("shipping-published", plain), out::add))
        assertTrue(out.joinToString("\n").contains("Titanic"))
    }

    @Test
    fun recordWithoutEventTypeHeaderIsShownAsUnknown() {
        val out = mutableListOf<String>()
        val record = ConsumerRecord<String, String?>(
            "hexagonship-shipping", 0, 0L, 0L, TimestampType.NO_TIMESTAMP_TYPE, 0, 0,
            "key", "raw-content", RecordHeaders(), Optional.empty()
        )

        assertFalse(handleRecord(record, out::add))
        assertEquals(1, out.size, out.toString())
        assertTrue(out[0].contains("raw-content"), out[0])
    }

    @Test
    fun shippingPublishedRecordWithoutValueIsReportedAndSkipped() {
        val out = mutableListOf<String>()

        assertFalse(handleRecord(record("shipping-published", null), out::add))
        assertEquals(1, out.size, out.toString())
        assertTrue(out[0].contains("Shipping Published"), out[0])
    }

    private fun sample(name: String): String =
        javaClass.getResourceAsStream("/events/$name.json")!!.readBytes().toString(Charsets.UTF_8)
}

fun record(eventType: String, value: String?, offset: Long = 0L) =
    ConsumerRecord<String, String?>(
        "hexagonship-shipping", 0, offset, 0L, TimestampType.NO_TIMESTAMP_TYPE, 0, 0,
        "key", value,
        RecordHeaders().add(RecordHeader("eventType", eventType.toByteArray(Charsets.UTF_8))),
        Optional.empty()
    )
