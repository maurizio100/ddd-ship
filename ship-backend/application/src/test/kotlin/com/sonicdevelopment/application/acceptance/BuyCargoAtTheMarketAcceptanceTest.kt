package com.sonicdevelopment.application.acceptance

import com.fasterxml.jackson.databind.ObjectMapper
import com.sonicdevelopment.application.acceptance.fixtures.FakeDrivenPorts
import com.sonicdevelopment.application.acceptance.fixtures.FakeHarborTest
import com.sonicdevelopment.application.acceptance.fixtures.SeedData
import com.sonicdevelopment.application.acceptance.fixtures.aShipBeingPrepared
import com.sonicdevelopment.application.acceptance.fixtures.givenPriceOf
import com.sonicdevelopment.application.acceptance.fixtures.givenSavings
import com.sonicdevelopment.application.acceptance.fixtures.givenStockOf
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestPropertySource
import java.util.*

/**
 * Feature: Buy Cargo at the Market.
 *
 * Background: the Price of Ale is 50.00 $ and the Stock holds 1 Ale. The User buys through
 * `POST /web/market/purchases`; Stock is read from `GET /web/stock`, Savings from `GET /web/savings`.
 */
@FakeHarborTest
@TestPropertySource(properties = ["harbor.name=Tortuga"])
class BuyCargoAtTheMarketAcceptanceTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var fakes: FakeDrivenPorts

    @BeforeEach
    fun background() {
        fakes.reset()
        // Given the Price of Ale is 50.00 $
        fakes.givenPriceOf("Ale", "50.00")
        // And the Stock holds 1 Ale
        fakes.givenStockOf("Ale", 1)
    }

    @Test
    fun `Buying Cargo adds it to the Stock and pays from the Savings`() {
        // Given the Savings are 150.00 $
        fakes.givenSavings("150.00")

        // When the User buys 2 Ale at the Market
        val purchase = buy("Ale", 2)

        // Then the Stock holds 3 Ale
        purchase.statusCode shouldBe HttpStatus.NO_CONTENT
        stockOf("Ale") shouldBe 3
        // And the Savings are 50.00 $
        savings() shouldBe "50.00"
    }

    @Test
    fun `Buying with exactly enough Savings empties them`() {
        // Given the Savings are 100.00 $
        fakes.givenSavings("100.00")

        // When the User buys 2 Ale at the Market
        val purchase = buy("Ale", 2)

        // Then the Stock holds 3 Ale
        purchase.statusCode shouldBe HttpStatus.NO_CONTENT
        stockOf("Ale") shouldBe 3
        // And the Savings are 0.00 $
        savings() shouldBe "0.00"
    }

    @Test
    fun `A purchase the Savings cannot cover is refused`() {
        // Given the Savings are 80.00 $
        fakes.givenSavings("80.00")

        // When the User buys 2 Ale at the Market
        val purchase = buy("Ale", 2)

        // Then the User is told that the Savings do not cover 100.00 $
        purchase.statusCode shouldBe HttpStatus.CONFLICT
        purchase.body!!["detail"] shouldBe "The Savings do not cover 100.00 $"
        // And the Stock holds 1 Ale
        stockOf("Ale") shouldBe 1
        // And the Savings are 80.00 $
        savings() shouldBe "80.00"
    }

    @Test
    fun `Bought Cargo can be loaded onto a ship`() {
        // Given the Savings are 150.00 $ and the User bought 2 Ale at the Market
        fakes.givenSavings("150.00")
        buy("Ale", 2).statusCode shouldBe HttpStatus.NO_CONTENT
        // And a ship with a new Shipping
        val shipId = restTemplate.aShipBeingPrepared()

        // When the User loads Ale onto that ship
        val load = restTemplate.postForEntity(
            "/web/ships/$shipId/cargos", mapOf("cargoId" to SeedData.cargoIdOf("Ale")), Map::class.java
        )

        // Then Ale is among the Loaded Cargo
        load.statusCode shouldBe HttpStatus.OK
        (load.body!!["cargo"] as List<*>).map { (it as Map<*, *>)["name"] as String } shouldContain "Ale"
        stockOf("Ale") shouldBe 2
    }

    private fun buy(cargoName: String, quantity: Int) =
        restTemplate.postForEntity(
            "/web/market/purchases",
            mapOf("cargoId" to SeedData.cargoIdOf(cargoName), "quantity" to quantity),
            Map::class.java
        )

    private fun stockOf(cargoName: String): Int {
        val response = restTemplate.getForEntity("/web/stock", String::class.java)
        response.statusCode shouldBe HttpStatus.OK
        val stock = ObjectMapper().readValue(response.body!!, List::class.java).map { it as Map<*, *> }
        return stock.single { it["name"] == cargoName }["quantity"] as Int
    }

    private fun savings(): String {
        val response = restTemplate.getForEntity("/web/savings", String::class.java)
        response.statusCode shouldBe HttpStatus.OK
        return ObjectMapper().readValue(response.body!!, Map::class.java)["amount"] as String
    }
}
