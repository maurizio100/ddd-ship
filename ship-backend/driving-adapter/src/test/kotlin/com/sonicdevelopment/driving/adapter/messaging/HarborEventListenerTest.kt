package com.sonicdevelopment.driving.adapter.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import com.sonicdevelopment.domain.model.values.EventId
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.ports.driving.harbor.HarborManagementPort
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.apache.kafka.common.header.internals.RecordHeaders
import org.apache.kafka.common.record.TimestampType
import org.junit.jupiter.api.Test
import java.util.*

class HarborEventListenerTest {

    private val harborManagementPort = mockk<HarborManagementPort>(relaxed = true)
    private val listener = HarborEventListener(InboundMessageReader(ObjectMapper()), harborManagementPort)

    @Test
    fun `delegates harbor-opened to learnAboutHarbor`() {
        val eventId = UUID.randomUUID()

        listener.onHarborEvent(aRecord(id = eventId, payload = """{"harborName":"Port Royal"}"""))

        verify(exactly = 1) { harborManagementPort.learnAboutHarbor(EventId(eventId), HarborName("Port Royal")) }
    }

    @Test
    fun `ignores other event types`() {
        listener.onHarborEvent(aRecord(eventType = "harbor-renamed", payload = """{"harborName":"Port Royal"}"""))

        confirmVerified(harborManagementPort)
    }

    @Test
    fun `ignores unknown payload fields`() {
        val eventId = UUID.randomUUID()

        listener.onHarborEvent(
            aRecord(id = eventId, payload = """{"harborName":"Nassau","flag":"Jolly Roger"}""")
        )

        verify(exactly = 1) { harborManagementPort.learnAboutHarbor(EventId(eventId), HarborName("Nassau")) }
    }

    private fun aRecord(
        id: UUID = UUID.randomUUID(),
        eventType: String = "harbor-opened",
        payload: String,
    ): ConsumerRecord<String, String> {
        val headers = RecordHeaders()
        headers.add(RecordHeader("id", id.toString().toByteArray(Charsets.UTF_8)))
        headers.add(RecordHeader("eventType", eventType.toByteArray(Charsets.UTF_8)))
        return ConsumerRecord(
            "hexagonship-harbor", 0, 0L, 0L, TimestampType.CREATE_TIME,
            0, 0, UUID.randomUUID().toString(), ObjectMapper().writeValueAsString(payload), headers, Optional.empty()
        )
    }
}
