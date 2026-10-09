package com.sonicdevelopment.application.acceptance

import com.fasterxml.jackson.databind.ObjectMapper
import com.sonicdevelopment.application.acceptance.fixtures.FakeDrivenPorts
import com.sonicdevelopment.application.acceptance.fixtures.FakeHarborTest
import com.sonicdevelopment.application.acceptance.fixtures.SeedData
import com.sonicdevelopment.application.acceptance.fixtures.aShippingPublishedRecord
import com.sonicdevelopment.application.acceptance.fixtures.givenPriceOf
import com.sonicdevelopment.application.acceptance.fixtures.givenSavings
import com.sonicdevelopment.driving.adapter.messaging.ShippingEventListener
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestPropertySource
import java.util.*

/**
 * Feature: Unload an Incoming Ship and pay the Delivery Price.
 *
 * The Harbor is "Port Royal", on in-memory fakes. "Salty Whisker" arrives through the [ShippingEventListener]
 * (as in the Incoming Ship feature) and is unloaded with `POST /web/incoming-ships/{id}/unloading`. State is
 * read over HTTP: `GET /web/incoming-ships`, `/web/ships`, `/web/stock` and `/web/savings`.
 *
 * Background: the Price of Rum is 40.00 $ and of Sugar 35.00 $ (Delivery Price 115.00 $); "Salty Whisker" was
 * Released to this Harbor with 2 Rum and 1 Sugar aboard and has arrived.
 */
@FakeHarborTest
@TestPropertySource(properties = ["harbor.name=Port Royal"])
class UnloadAnIncomingShipAcceptanceTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var fakes: FakeDrivenPorts

    @Autowired
    lateinit var shippingEventListener: ShippingEventListener

    private val saltyWhiskerId: UUID = UUID.randomUUID()

    @BeforeEach
    fun background() {
        fakes.reset()
        fakes.givenPriceOf("Rum", "40.00")
        fakes.givenPriceOf("Sugar", "35.00")
        val rum = SeedData.cargoIdOf("Rum")
        val sugar = SeedData.cargoIdOf("Sugar")
        shippingEventListener.onShippingEvent(
            aShippingPublishedRecord(
                shipId = saltyWhiskerId,
                shipName = "Salty Whisker",
                catainId = SeedData.aCatainId,
                shippingId = UUID.randomUUID(),
                cargoIds = listOf(rum, rum, sugar),
                originHarbor = "Tortuga",
                destinationHarbor = "Port Royal",
            )
        )
    }

    @Test
    fun `Unloading puts the Cargo into the Stock and pays the Delivery Price`() {
        // Given the Savings are 1000.00 $
        fakes.givenSavings("1000.00")
        val stockBefore = stock()

        // When the User unloads "Salty Whisker"
        val unloading = unload()

        // Then the Stock grows by 2 Rum and 1 Sugar
        unloading.statusCode shouldBe HttpStatus.NO_CONTENT
        val stockAfter = stock()
        stockAfter["Rum"] shouldBe stockBefore["Rum"]!! + 2
        stockAfter["Sugar"] shouldBe stockBefore["Sugar"]!! + 1
        // And the Savings are 885.00 $
        savings() shouldBe "885.00"
        // And "Salty Whisker" is no longer an Incoming Ship
        incomingShips().none { it["shipId"] == saltyWhiskerId.toString() } shouldBe true
        ships().single { it["id"] == saltyWhiskerId.toString() }["incoming"] shouldBe false
    }

    @Test
    fun `An Incoming Ship cannot be unloaded when the Savings fall short`() {
        // Given the Savings are 50.00 $
        fakes.givenSavings("50.00")
        val stockBefore = stock()

        // When the User unloads "Salty Whisker"
        val unloading = unload()

        // Then the User is told that the Savings do not cover the Delivery Price of 115.00 $
        unloading.statusCode shouldBe HttpStatus.CONFLICT
        unloading.body!!["detail"] shouldBe "The Savings do not cover the Delivery Price of 115.00 $"
        // And its Cargo is still aboard
        val incoming = incomingShips().single { it["shipId"] == saltyWhiskerId.toString() }
        (incoming["cargo"] as List<*>).map { (it as Map<*, *>)["name"] as String }.sorted() shouldBe
            listOf("Rum", "Rum", "Sugar")
        incomingShips() shouldHaveSize 1
        ships().single { it["id"] == saltyWhiskerId.toString() }["incoming"] shouldBe true
        stock() shouldBe stockBefore
        // And the Savings are 50.00 $
        savings() shouldBe "50.00"
    }

    private fun unload() =
        restTemplate.postForEntity("/web/incoming-ships/$saltyWhiskerId/unloading", null, Map::class.java)

    private fun savings(): String {
        val response = restTemplate.getForEntity("/web/savings", String::class.java)
        response.statusCode shouldBe HttpStatus.OK
        return ObjectMapper().readValue(response.body!!, Map::class.java)["amount"] as String
    }

    private fun stock(): Map<String, Int> =
        getList("/web/stock").associate { it["name"] as String to it["quantity"] as Int }

    private fun ships(): List<Map<*, *>> = getList("/web/ships")

    private fun incomingShips(): List<Map<*, *>> = getList("/web/incoming-ships")

    private fun getList(url: String): List<Map<*, *>> {
        val response = restTemplate.getForEntity(url, String::class.java)
        response.statusCode shouldBe HttpStatus.OK
        return ObjectMapper().readValue(response.body!!, List::class.java).map { it as Map<*, *> }
    }
}
