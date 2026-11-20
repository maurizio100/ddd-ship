import org.example.handleRecord
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EventAnnouncementTest {

    private val shipId = "550e8400-e29b-41d4-a716-446655440000"
    private val shippingId = "550e8400-e29b-41d4-a716-446655440001"

    // Outbox and Coast Guard write the payload as a JSON string literal.
    private fun literal(json: String) = "\"" + json.replace("\\", "\\\\").replace("\"", "\\\"") + "\""

    @Test
    fun shipArrivedIsAnnouncedAsArrival() {
        val out = mutableListOf<String>()
        val json = """{"shipId":"$shipId","shipName":"Black Pearl","shippingId":"$shippingId","originHarbor":"Tortuga","destinationHarbor":"Port Royal"}"""

        assertTrue(handleRecord(record("ship-arrived", literal(json)), out::add))

        assertEquals(1, out.size, out.toString())
        val line = out[0]
        assertTrue(line.contains("Black Pearl"), line)
        assertTrue(line.contains("arrived at Port Royal from Tortuga"), line)
    }

    @Test
    fun harborOpenedIsAnnounced() {
        val out = mutableListOf<String>()

        assertTrue(handleRecord(record("harbor-opened", literal("""{"harborName":"Isla de Muerta"}""")), out::add))

        assertEquals(1, out.size, out.toString())
        assertTrue(out[0].contains("Isla de Muerta"), out[0])
        assertTrue(out[0].contains("opened"), out[0])
    }

    @Test
    fun shipImprisonedIsAnnounced() {
        val out = mutableListOf<String>()
        val json = """{"shipId":"$shipId","shipName":"Black Pearl","holdingHarbor":"Port Royal","homeHarbor":"Tortuga","shippingId":"$shippingId"}"""

        assertTrue(handleRecord(record("ship-imprisoned", literal(json)), out::add))

        assertEquals(1, out.size, out.toString())
        assertTrue(out[0].contains("Black Pearl is imprisoned at Port Royal"), out[0])
        assertTrue(out[0].contains("Home Harbor is Tortuga"), out[0])
    }

    @Test
    fun unknownKindIsShownAsReceivedAndLoopContinues() {
        val out = mutableListOf<String>()

        val unknown = handleRecord(record("kraken-sighted", "{\"depth\":42}", offset = 0L), out::add)
        val next = handleRecord(
            record("harbor-opened", literal("""{"harborName":"Isla de Muerta"}"""), offset = 1L), out::add
        )

        assertFalse(unknown)
        assertEquals(2, out.size, out.toString())
        assertTrue(out[0].contains("kraken-sighted"), out[0])
        assertTrue(out[0].contains("{\"depth\":42}"), out[0])
        assertFalse(out[0].contains("\n"), out[0])
        assertTrue(next)
        assertTrue(out[1].contains("Isla de Muerta"), out[1])
    }

    @Test
    fun unreadableShipArrivedIsReportedAndSkipped() {
        val out = mutableListOf<String>()

        val unreadable = handleRecord(record("ship-arrived", "not json at all {", offset = 0L), out::add)
        val next = handleRecord(
            record("harbor-opened", literal("""{"harborName":"Isla de Muerta"}"""), offset = 1L), out::add
        )

        assertFalse(unreadable)
        assertEquals(2, out.size, out.toString())
        assertTrue(out[0].contains("Ship Arrived"), out[0])
        assertFalse(out[0].contains("\n"), out[0])
        assertTrue(next)
        assertTrue(out[1].contains("Isla de Muerta"), out[1])
    }
}
