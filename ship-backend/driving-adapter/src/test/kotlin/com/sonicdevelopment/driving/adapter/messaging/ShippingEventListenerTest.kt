package com.sonicdevelopment.driving.adapter.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import com.sonicdevelopment.domain.model.values.CargoId
import com.sonicdevelopment.domain.model.values.CatainId
import com.sonicdevelopment.domain.model.values.EventId
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.model.values.ShippingId
import com.sonicdevelopment.domain.ports.driving.shipping.ArrivalManagementPort
import com.sonicdevelopment.domain.ports.driving.shipping.ShipArrivedDTO
import com.sonicdevelopment.domain.ports.driving.shipping.ShippingPublishedDTO
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
    fun `propagates a failure of the Arrival so the record is redelivered`() {
        every { arrivalManagementPort.receiveShippingPublished(any(), any()) } throws
            IllegalStateException("unknown Catain")

        shouldThrow<IllegalStateException> {
            listener.onShippingEvent(aRecord(payload = aShippingPublishedPayload()))
        }
    }

    @Test
    fun `delegates ship-arrived to receiveShipArrived`() {
        val eventId = UUID.randomUUID()

        listener.onShippingEvent(
            aRecord(
                id = eventId,
                eventType = "ship-arrived",
                payload = """{"shipId":"$shipId","shipName":"Black Pearl","shippingId":"$shippingId",""" +
                    """"originHarbor":"Tortuga","destinationHarbor":"Port Royal"}"""
            )
        )

        verify(exactly = 1) {
            arrivalManagementPort.receiveShipArrived(
                EventId(eventId),
                ShipArrivedDTO(
                    shipId = ShipId(shipId),
                    shippingId = ShippingId(shippingId),
                    originHarbor = HarborName("Tortuga"),
                    destinationHarbor = HarborName("Port Royal"),
                )
            )
        }
        confirmVerified(arrivalManagementPort)
    }

    @Test
    fun `ignores other event types`() {
        listener.onShippingEvent(aRecord(eventType = "harbor-opened", payload = """{"harborName":"Nassau"}"""))

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

    @Test
    fun `maps the Home Harbor of the ship, and a missing or blank one to null`() {
        val withHomeHarbor = UUID.randomUUID()
        val withoutHomeHarbor = UUID.randomUUID()
        val withBlankHomeHarbor = UUID.randomUUID()

        listener.onShippingEvent(aRecord(id = withHomeHarbor, payload = aShippingPublishedPayload(homeHarbor = "\"Isla de Muerta\"")))
        listener.onShippingEvent(aRecord(id = withoutHomeHarbor, payload = aShippingPublishedPayload()))
        listener.onShippingEvent(aRecord(id = withBlankHomeHarbor, payload = aShippingPublishedPayload(homeHarbor = "\" \"")))

        val published = ShippingPublishedDTO(
            shipId = ShipId(shipId),
            shipName = "Black Pearl",
            catainId = CatainId(catainId),
            shippingId = ShippingId(shippingId),
            cargoIds = listOf(CargoId(rumId), CargoId(silkId)),
            originHarbor = HarborName("Tortuga"),
            destinationHarbor = HarborName("Port Royal"),
        )
        verify(exactly = 1) {
            arrivalManagementPort.receiveShippingPublished(
                EventId(withHomeHarbor), published.copy(homeHarbor = HarborName("Isla de Muerta"))
            )
            arrivalManagementPort.receiveShippingPublished(EventId(withoutHomeHarbor), published.copy(homeHarbor = null))
            arrivalManagementPort.receiveShippingPublished(EventId(withBlankHomeHarbor), published.copy(homeHarbor = null))
        }
    }

    @Test
    fun `a shipping-published with earnings maps to Money`() {
        val eventId = UUID.randomUUID()

        listener.onShippingEvent(aRecord(id = eventId, payload = aShippingPublishedPayload(earnings = "\"80.00\"")))

        verify(exactly = 1) {
            arrivalManagementPort.receiveShippingPublished(EventId(eventId), match { it.earnings == Money.of("80.00") })
        }
    }

    @Test
    fun `a shipping-published without earnings maps to no Earnings`() {
        val withoutEarnings = UUID.randomUUID()
        val withBlankEarnings = UUID.randomUUID()

        listener.onShippingEvent(aRecord(id = withoutEarnings, payload = aShippingPublishedPayload()))
        listener.onShippingEvent(aRecord(id = withBlankEarnings, payload = aShippingPublishedPayload(earnings = "\" \"")))

        verify(exactly = 1) {
            arrivalManagementPort.receiveShippingPublished(EventId(withoutEarnings), match { it.earnings == Money.dollars(0) })
            arrivalManagementPort.receiveShippingPublished(EventId(withBlankEarnings), match { it.earnings == Money.dollars(0) })
        }
    }

    /** [homeHarbor] and [earnings] are JSON values written into `shipEventData`, or left out when `null`. */
    private fun aShippingPublishedPayload(homeHarbor: String? = null, earnings: String? = null) = """
        {"shipEventData":{"shipId":"$shipId","shipName":"Black Pearl"${homeHarbor?.let { ",\"homeHarbor\":$it" } ?: ""}${earnings?.let { ",\"earnings\":$it" } ?: ""}},
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
