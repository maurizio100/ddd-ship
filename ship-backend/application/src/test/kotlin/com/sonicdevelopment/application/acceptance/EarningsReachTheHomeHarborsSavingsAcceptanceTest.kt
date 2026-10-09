package com.sonicdevelopment.application.acceptance

import com.fasterxml.jackson.databind.ObjectMapper
import com.sonicdevelopment.application.acceptance.fixtures.FakeDrivenPorts
import com.sonicdevelopment.application.acceptance.fixtures.FakeHarborTest
import com.sonicdevelopment.application.acceptance.fixtures.SeedData
import com.sonicdevelopment.application.acceptance.fixtures.aShippingPublishedRecord
import com.sonicdevelopment.application.acceptance.fixtures.givenPriceOf
import com.sonicdevelopment.application.acceptance.fixtures.givenSavings
import com.sonicdevelopment.driving.adapter.messaging.ShippingEventListener
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestPropertySource
import java.util.*

/**
 * Feature: Earnings reach the Home Harbor's Savings.
 *
 * On in-memory fakes. "Salty Whisker", Home Harbor "Port Royal", carrying Earnings of 80.00 $, arrives through the
 * [ShippingEventListener]. State is read over HTTP (`GET /web/ships`, `/web/savings`). Background: the Price of Rum
 * is 40.00 $.
 */
class EarningsReachTheHomeHarborsSavingsAcceptanceTest {

    @FakeHarborTest
    abstract class AHarbor {
        @Autowired
        lateinit var restTemplate: TestRestTemplate

        @Autowired
        lateinit var fakes: FakeDrivenPorts

        @Autowired
        lateinit var shippingEventListener: ShippingEventListener

        val saltyWhiskerId: UUID = UUID.randomUUID()

        @BeforeEach
        fun background() {
            fakes.reset()
            fakes.givenPriceOf("Rum", "40.00")
        }

        /** The record of "Salty Whisker" (Home Harbor "Port Royal", Earnings 80.00 $) arriving at [destination]. */
        fun saltyWhiskerArrivalRecord(
            destination: String,
            shippingId: UUID = UUID.randomUUID(),
            eventId: UUID = UUID.randomUUID(),
        ) = aShippingPublishedRecord(
            shipId = saltyWhiskerId,
            shipName = "Salty Whisker",
            catainId = SeedData.aCatainId,
            shippingId = shippingId,
            cargoIds = emptyList(),
            originHarbor = "Somewhere",
            destinationHarbor = destination,
            homeHarbor = "Port Royal",
            earnings = "80.00",
            eventId = eventId,
        )

        fun earningsOfSaltyWhisker(): Any? {
            val response = restTemplate.getForEntity("/web/ships", String::class.java)
            response.statusCode shouldBe HttpStatus.OK
            return ObjectMapper().readValue(response.body!!, List::class.java)
                .map { it as Map<*, *> }
                .single { it["id"] == saltyWhiskerId.toString() }["earnings"]
        }

        fun savings(): String {
            val response = restTemplate.getForEntity("/web/savings", String::class.java)
            response.statusCode shouldBe HttpStatus.OK
            return ObjectMapper().readValue(response.body!!, Map::class.java)["amount"] as String
        }
    }

    @Nested
    @TestPropertySource(properties = ["harbor.name=Port Royal"])
    inner class AtPortRoyal : AHarbor() {

        @Test
        fun `Earnings go into the Home Harbor's Savings when the ship arrives there`() {
            // Given "Salty Whisker" carries Earnings of 80.00 $
            // And the Savings of "Port Royal" are 1000.00 $
            fakes.givenSavings("1000.00")

            // When "Salty Whisker" arrives at "Port Royal"
            shippingEventListener.onShippingEvent(saltyWhiskerArrivalRecord("Port Royal"))

            // Then the Savings of "Port Royal" are 1080.00 $
            savings() shouldBe "1080.00"
            // And "Salty Whisker" carries no Earnings
            earningsOfSaltyWhisker() shouldBe "0.00"
        }

        @Test
        fun `An Arrival reported twice credits the Earnings once`() {
            // Given "Salty Whisker" carries Earnings of 80.00 $
            // And the Savings of "Port Royal" are 1000.00 $
            fakes.givenSavings("1000.00")

            // When the Arrival is reported twice: the same event redelivered, and a re-publication of the same
            // Release under a new event id
            val shippingId = UUID.randomUUID()
            val record = saltyWhiskerArrivalRecord("Port Royal", shippingId = shippingId)
            shippingEventListener.onShippingEvent(record)
            shippingEventListener.onShippingEvent(record)
            shippingEventListener.onShippingEvent(
                saltyWhiskerArrivalRecord("Port Royal", shippingId = shippingId, eventId = UUID.randomUUID())
            )

            // Then the Savings of "Port Royal" are 1080.00 $
            savings() shouldBe "1080.00"
        }
    }

    @Nested
    @TestPropertySource(properties = ["harbor.name=Isla de Muerta"])
    inner class AtIslaDeMuerta : AHarbor() {

        @Test
        fun `Earnings are not credited at a Harbor that is not the Home Harbor`() {
            // Given "Salty Whisker" carries Earnings of 80.00 $
            fakes.givenSavings("1000.00")

            // When "Salty Whisker" arrives at "Isla de Muerta"
            shippingEventListener.onShippingEvent(saltyWhiskerArrivalRecord("Isla de Muerta"))

            // Then the Savings of "Isla de Muerta" are unchanged
            savings() shouldBe "1000.00"
            // And "Salty Whisker" still carries Earnings of 80.00 $
            earningsOfSaltyWhisker() shouldBe "80.00"
        }
    }
}
