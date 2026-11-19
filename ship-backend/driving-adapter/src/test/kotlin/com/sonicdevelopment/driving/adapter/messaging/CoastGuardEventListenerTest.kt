package com.sonicdevelopment.driving.adapter.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import com.sonicdevelopment.domain.model.values.EventId
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.ports.driving.shipping.ImprisonmentManagementPort
import com.sonicdevelopment.domain.ports.driving.shipping.ShipImprisonedDTO
import io.kotest.assertions.throwables.shouldThrow
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.apache.kafka.common.header.internals.RecordHeaders
import org.apache.kafka.common.record.TimestampType
import org.junit.jupiter.api.Test
import java.util.*

class CoastGuardEventListenerTest {

    private val imprisonmentManagementPort = mockk<ImprisonmentManagementPort>(relaxed = true)
    private val listener = CoastGuardEventListener(InboundMessageReader(ObjectMapper()), imprisonmentManagementPort)

    private val shipId = UUID.randomUUID()
    private val saltyWhisker = ShipImprisonedDTO(
        shipId = ShipId(shipId), shipName = "Salty Whisker",
        holdingHarbor = HarborName("Port Royal"), homeHarbor = HarborName("Tortuga"),
    )

    @Test
    fun `delegates ship-imprisoned to receiveShipImprisoned`() {
        val eventId = UUID.randomUUID()

        listener.onCoastGuardEvent(aRecord(id = eventId, payload = aShipImprisonedPayload()))

        verify(exactly = 1) { imprisonmentManagementPort.receiveShipImprisoned(EventId(eventId), saltyWhisker) }
    }

    @Test
    fun `ignores other event types`() {
        listener.onCoastGuardEvent(aRecord(eventType = "fine-paid", payload = aShipImprisonedPayload()))

        confirmVerified(imprisonmentManagementPort)
    }

    @Test
    fun `ignores unknown payload fields`() {
        val eventId = UUID.randomUUID()

        listener.onCoastGuardEvent(aRecord(id = eventId, payload = aShipImprisonedPayload(extra = ""","fine":"120.00"""")))

        verify(exactly = 1) { imprisonmentManagementPort.receiveShipImprisoned(EventId(eventId), saltyWhisker) }
    }

    @Test
    fun `propagates a failure of the imprisonment so the record is redelivered`() {
        every { imprisonmentManagementPort.receiveShipImprisoned(any(), any()) } throws IllegalStateException("db down")

        shouldThrow<IllegalStateException> {
            listener.onCoastGuardEvent(aRecord(payload = aShipImprisonedPayload()))
        }
    }

    private fun aShipImprisonedPayload(extra: String = "") =
        """{"shipId":"$shipId","shipName":"Salty Whisker","holdingHarbor":"Port Royal","homeHarbor":"Tortuga","shippingId":"${UUID.randomUUID()}"$extra}"""

    private fun aRecord(
        id: UUID = UUID.randomUUID(),
        eventType: String = "ship-imprisoned",
        payload: String,
    ): ConsumerRecord<String, String> {
        val headers = RecordHeaders()
        headers.add(RecordHeader("id", id.toString().toByteArray(Charsets.UTF_8)))
        headers.add(RecordHeader("eventType", eventType.toByteArray(Charsets.UTF_8)))
        return ConsumerRecord(
            CoastGuardEventListener.COAST_GUARD_TOPIC, 0, 0L, 0L, TimestampType.CREATE_TIME,
            0, 0, shipId.toString(), ObjectMapper().writeValueAsString(payload), headers, Optional.empty()
        )
    }
}
