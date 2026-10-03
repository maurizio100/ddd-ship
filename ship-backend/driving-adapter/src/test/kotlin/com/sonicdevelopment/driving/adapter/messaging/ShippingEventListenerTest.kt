package com.sonicdevelopment.driving.adapter.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import com.sonicdevelopment.domain.model.values.CargoId
import com.sonicdevelopment.domain.model.values.CatainId
import com.sonicdevelopment.domain.model.values.EventId
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.model.values.ShippingId
import com.sonicdevelopment.domain.ports.driving.shipping.ArrivalManagementPort
import com.sonicdevelopment.domain.ports.driving.shipping.ShippingPublishedDTO
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.apache.kafka.common.header.internals.RecordHeaders
import org.apache.kafka.common.record.TimestampType
import org.junit.jupiter.api.Test
import java.util.*

class ShippingEventListenerTest {

    private val arrivalManagementPort = mockk<ArrivalManagementPort>(relaxed = true)
    private val listener = ShippingEventListener(InboundMessageReader(ObjectMapper()), arrivalManagementPort)

    private val shipId = UUID.randomUUID()
    private val catainId = UUID.randomUUID()
    private val shippingId = UUID.randomUUID()
    private val rumId = UUID.randomUUID()
    private val silkId = UUID.randomUUID()

    @Test
    fun `delegates shipping-published to receiveShippingPublished`() {
        val eventId = UUID.randomUUID()

        listener.onShippingEvent(aRecord(id = eventId, payload = aShippingPublishedPayload()))

        verify(exactly = 1) {
            arrivalManagementPort.receiveShippingPublished(
                EventId(eventId),
                ShippingPublishedDTO(
                    shipId = ShipId(shipId),
                    shipName = "Black Pearl",
                    catainId = CatainId(catainId),
                    shippingId = ShippingId(shippingId),
                    cargoIds = listOf(CargoId(rumId), CargoId(silkId)),
                    originHarbor = HarborName("Tortuga"),
                    destinationHarbor = HarborName("Port Royal"),
                )
            )
        }
    }

    @Test
    fun `ignores other event types (ship-arrived)`() {
        listener.onShippingEvent(
            aRecord(
                eventType = "ship-arrived",
                payload = """{"shipId":"$shipId","shipName":"Black Pearl","shippingId":"$shippingId",""" +
                    """"originHarbor":"Tortuga","destinationHarbor":"Port Royal"}"""
            )
        )

        confirmVerified(arrivalManagementPort)
    }

    @Test
    fun `ignores unknown payload fields and maps a missing Destination Harbor to null`() {
        val eventId = UUID.randomUUID()
        val payload = """
            {"shipEventData":{"shipId":"$shipId","shipName":"Black Pearl","flag":"Jolly Roger"},
             "shippingEventData":{"shippingId":"$shippingId","weight":7.5,"shippingQuote":"Fair winds",
               "cargo":[{"cargoId":"$rumId","cargoName":"Rum"}],"originHarbor":""},
             "catain":{"catainId":"$catainId","catainName":"Whiskers"},
             "parrot":"Polly"}
        """.trimIndent()

        listener.onShippingEvent(aRecord(id = eventId, payload = payload))

        verify(exactly = 1) {
            arrivalManagementPort.receiveShippingPublished(
                EventId(eventId),
                ShippingPublishedDTO(
                    shipId = ShipId(shipId),
                    shipName = "Black Pearl",
                    catainId = CatainId(catainId),
                    shippingId = ShippingId(shippingId),
                    cargoIds = listOf(CargoId(rumId)),
                    originHarbor = null,
                    destinationHarbor = null,
                )
            )
        }
    }

    private fun aShippingPublishedPayload() = """
        {"shipEventData":{"shipId":"$shipId","shipName":"Black Pearl"},
         "shippingEventData":{"shippingId":"$shippingId","weight":7.5,"shippingQuote":"Fair winds",
           "cargo":[{"cargoId":"$rumId","cargoName":"Rum"},{"cargoId":"$silkId","cargoName":"Silk"}],
           "originHarbor":"Tortuga","destinationHarbor":"Port Royal"},
         "catain":{"catainId":"$catainId","catainName":"Whiskers"}}
    """.trimIndent()

    private fun aRecord(
        id: UUID = UUID.randomUUID(),
        eventType: String = "shipping-published",
        payload: String,
    ): ConsumerRecord<String, String> {
        val headers = RecordHeaders()
        headers.add(RecordHeader("id", id.toString().toByteArray(Charsets.UTF_8)))
        headers.add(RecordHeader("eventType", eventType.toByteArray(Charsets.UTF_8)))
        return ConsumerRecord(
            "hexagonship-shipping", 0, 0L, 0L, TimestampType.CREATE_TIME,
            0, 0, shippingId.toString(), ObjectMapper().writeValueAsString(payload), headers, Optional.empty()
        )
    }
}
