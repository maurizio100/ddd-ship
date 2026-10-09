package com.sonicdevelopment.application.acceptance

import com.fasterxml.jackson.databind.ObjectMapper
import com.sonicdevelopment.application.acceptance.fixtures.FakeDrivenPorts
import com.sonicdevelopment.application.acceptance.fixtures.FakeHarborTest
import com.sonicdevelopment.application.acceptance.fixtures.SeedData
import com.sonicdevelopment.application.acceptance.fixtures.aShippingPublishedRecord
import com.sonicdevelopment.application.acceptance.fixtures.givenKnownHarbors
import com.sonicdevelopment.application.acceptance.fixtures.givenPriceOf
import com.sonicdevelopment.application.acceptance.fixtures.givenSavings
import com.sonicdevelopment.application.acceptance.fixtures.release
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
 * Feature: Unloading earns the ship its Earnings.
 *
 * On in-memory fakes. "Salty Whisker", Home Harbor "Port Royal", arrives through the [ShippingEventListener]
 * and is unloaded with `POST /web/incoming-ships/{id}/unloading`. State is read over HTTP (`GET /web/ships`,
 * `/web/savings`) and from the fake outbox. Background: the Price of Rum is 40.00 $.
 */
class UnloadingEarnsTheShipItsEarningsAcceptanceTest {

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

        /** "Salty Whisker", Home Harbor "Port Royal", arrived with [rumAboard] Rum, carrying [earnings] if given. */
        fun saltyWhiskerArrives(rumAboard: Int, earnings: String?, destination: String) {
            val rum = SeedData.cargoIdOf("Rum")
            shippingEventListener.onShippingEvent(
                aShippingPublishedRecord(
                    shipId = saltyWhiskerId,
                    shipName = "Salty Whisker",
                    catainId = SeedData.aCatainId,
                    shippingId = UUID.randomUUID(),
                    cargoIds = List(rumAboard) { rum },
                    originHarbor = "Somewhere",
                    destinationHarbor = destination,
                    homeHarbor = "Port Royal",
                    earnings = earnings,
                )
            )
        }

        fun unload() {
            restTemplate.postForEntity("/web/incoming-ships/$saltyWhiskerId/unloading", null, Map::class.java)
                .statusCode shouldBe HttpStatus.NO_CONTENT
        }

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
    @TestPropertySource(properties = ["harbor.name=Tortuga"])
    inner class AtTortuga : AHarbor() {

        @Test
        fun `The Delivery Price paid on unloading becomes the ship's Earnings`() {
            // Given "Salty Whisker" arrived at "Tortuga" with 2 Rum aboard and no Earnings
            fakes.givenKnownHarbors("Isla de Muerta")
            fakes.givenSavings("1000.00")
            saltyWhiskerArrives(rumAboard = 2, earnings = null, destination = "Tortuga")

            // When the User at "Tortuga" unloads "Salty Whisker"
            unload()

            // Then "Salty Whisker" carries Earnings of 80.00 $
            earningsOfSaltyWhisker() shouldBe "80.00"
            // And the Earnings travel with the ship when it is Released again
            restTemplate.postForEntity("/web/ships/$saltyWhiskerId/shippings", null, Map::class.java)
                .statusCode shouldBe HttpStatus.OK
            restTemplate.release(saltyWhiskerId, "Isla de Muerta").statusCode shouldBe HttpStatus.OK
            fakes.outbox.shippingPublished().single().earnings shouldBe "80.00"
        }
    }

    @Nested
    @TestPropertySource(properties = ["harbor.name=Isla de Muerta"])
    inner class AtIslaDeMuerta : AHarbor() {

        @Test
        fun `Earnings collect over several deliveries`() {
            // Given "Salty Whisker" carries Earnings of 80.00 $
            // And "Salty Whisker" arrived at "Isla de Muerta" with 1 Rum aboard
            fakes.givenSavings("1000.00")
            saltyWhiskerArrives(rumAboard = 1, earnings = "80.00", destination = "Isla de Muerta")
            earningsOfSaltyWhisker() shouldBe "80.00"

            // When the User at "Isla de Muerta" unloads "Salty Whisker"
            unload()

            // Then "Salty Whisker" carries Earnings of 120.00 $
            earningsOfSaltyWhisker() shouldBe "120.00"
        }
    }

    @Nested
    @TestPropertySource(properties = ["harbor.name=Port Royal"])
    inner class AtPortRoyal : AHarbor() {

        @Test
        fun `Unloading at the Home Harbor credits the Earnings at once`() {
            // Given "Salty Whisker" arrived at "Port Royal" with 2 Rum aboard and no Earnings
            // And the Savings of "Port Royal" are 1000.00 $
            saltyWhiskerArrives(rumAboard = 2, earnings = null, destination = "Port Royal")
            fakes.givenSavings("1000.00")

            // When the User at "Port Royal" unloads "Salty Whisker"
            unload()

            // Then the Savings of "Port Royal" are 1000.00 $
            savings() shouldBe "1000.00"
            // And "Salty Whisker" carries no Earnings
            earningsOfSaltyWhisker() shouldBe "0.00"
        }
    }
}
