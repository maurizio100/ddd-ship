package com.sonicdevelopment.application.acceptance

import com.fasterxml.jackson.databind.ObjectMapper
import com.sonicdevelopment.application.acceptance.fixtures.FakeDrivenPorts
import com.sonicdevelopment.application.acceptance.fixtures.FakeHarborTest
import com.sonicdevelopment.application.acceptance.fixtures.SeedData
import com.sonicdevelopment.application.acceptance.fixtures.givenPriceOf
import com.sonicdevelopment.application.acceptance.fixtures.givenSavings
import com.sonicdevelopment.domain.ports.driving.harbor.HarborManagementPort
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestPropertySource
import java.math.BigDecimal

/**
 * Feature: Cargo has a Price and the Harbor has Savings.
 *
 * The User sees the Prices through `GET /web/stock` and the Savings through `GET /web/savings`. Money is a decimal
 * string with two decimals. "Opening" is `HarborManagementPort.openHarbor()`; a Harbor opened for the first time is
 * `fakes.reset()` (no Prices, the Starting Savings that `V15` seeds) followed by the opening.
 */
class CargoHasAPriceAndTheHarborHasSavingsAcceptanceTest {

    @Nested
    @TestPropertySource(properties = ["harbor.name=Tortuga"])
    inner class AtAHarbor : AHarbor() {

        @Test
        fun `A Harbor opened for the first time holds its Starting Savings`() {
            // Given a Harbor that has just opened for the first time
            fakes.reset()
            harborManagementPort.openHarbor()

            // When the User opens the harbor management page
            val savings = savings()

            // Then the Savings shown are 1000.00 $
            savings["amount"] shouldBe "1000.00"
        }

        @Test
        fun `A Harbor opened for the first time rolls a Price for every Cargo`() {
            // Given a Harbor that has just opened for the first time
            fakes.reset()
            harborManagementPort.openHarbor()

            // When the User opens the harbor management page
            val stock = stock()

            // Then every Cargo of the catalog is shown with a Price in whole dollars from 30.00 $ to 60.00 $
            stock.map { it["name"] as String } shouldContainExactlyInAnyOrder SeedData.allCargo().map { it.name }
            stock.forEach {
                it["price"].shouldNotBeNull()
                val price = BigDecimal(it["price"] as String)
                price.scale() shouldBe 2
                price.remainder(BigDecimal.ONE).signum() shouldBe 0
                price shouldBeGreaterThanOrEqualTo BigDecimal("30.00")
                price shouldBeLessThanOrEqualTo BigDecimal("60.00")
            }
        }

        @Test
        fun `A Harbor opening again keeps its Prices`() {
            // Given a Harbor where the Price of Rum is 42.00 $
            fakes.reset()
            harborManagementPort.openHarbor()
            fakes.givenPriceOf("Rum", "42.00")
            val before = prices()
            before["Rum"] shouldBe "42.00"

            // When the Harbor opens again
            harborManagementPort.openHarbor()

            // Then the Price of Rum is still 42.00 $
            val after = prices()
            after["Rum"] shouldBe "42.00"
            // And no other Price changed
            after shouldBe before
        }

        @Test
        fun `A Harbor opening again keeps its Savings`() {
            // Given a Harbor whose Savings are 640.50 $
            fakes.givenSavings("640.50")

            // When the Harbor opens again
            harborManagementPort.openHarbor()

            // Then the Savings shown are 640.50 $
            savings()["amount"] shouldBe "640.50"
        }
    }

    @Nested
    @TestPropertySource(properties = ["harbor.name=Port Royal"])
    inner class AtPortRoyal : AHarbor() {

        @Test
        fun `Each Harbor has its own Prices`() {
            // Given the Harbor "Port Royal" rolled a Price of 42.00 $ for Rum
            fakes.givenPriceOf("Rum", "42.00")

            // When the User looks up the Price of Rum
            // Then the Price of Rum is 42.00 $ at "Port Royal"
            prices()["Rum"] shouldBe "42.00"
        }
    }

    @Nested
    @TestPropertySource(properties = ["harbor.name=Tortuga"])
    inner class AtTortuga : AHarbor() {

        @Test
        fun `Each Harbor has its own Prices`() {
            // Given the Harbor "Tortuga" rolled a Price of 57.00 $ for Rum
            fakes.givenPriceOf("Rum", "57.00")

            // When the User looks up the Price of Rum
            // Then the Price of Rum is 57.00 $ at "Tortuga"
            prices()["Rum"] shouldBe "57.00"
        }
    }

    /** One Harbor on in-memory fakes. */
    @FakeHarborTest
    abstract class AHarbor {

        @Autowired
        lateinit var restTemplate: TestRestTemplate

        @Autowired
        lateinit var fakes: FakeDrivenPorts

        @Autowired
        lateinit var harborManagementPort: HarborManagementPort

        @BeforeEach
        fun aFreshHarbor() {
            fakes.reset()
        }

        fun stock(): List<Map<*, *>> {
            val response = restTemplate.getForEntity("/web/stock", String::class.java)
            response.statusCode shouldBe HttpStatus.OK
            return ObjectMapper().readValue(response.body!!, List::class.java).map { it as Map<*, *> }
        }

        /** The Price of every Cargo by name, as the page shows it. */
        fun prices(): Map<String, Any?> = stock().associate { it["name"] as String to it["price"] }

        fun savings(): Map<*, *> {
            val response = restTemplate.getForEntity("/web/savings", String::class.java)
            response.statusCode shouldBe HttpStatus.OK
            return ObjectMapper().readValue(response.body!!, Map::class.java)
        }
    }
}
