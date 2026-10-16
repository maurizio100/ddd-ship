package com.sonicdevelopment.application.acceptance

import com.sonicdevelopment.application.acceptance.fixtures.FakeDrivenPorts
import com.sonicdevelopment.application.acceptance.fixtures.FakeHarborTest
import com.sonicdevelopment.application.acceptance.fixtures.SeedData
import com.sonicdevelopment.application.acceptance.fixtures.aShipBeingPrepared
import com.sonicdevelopment.application.acceptance.fixtures.givenStockOf
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestPropertySource

/**
 * Feature: The harbor management page shows the Harbor's Stock.
 *
 * The page reads the Stock through `GET /web/stock`: every catalog Cargo with its quantity, 0 included.
 * The scenario "The harbor management page is reachable from every screen" is a frontend concern.
 */
@FakeHarborTest
@TestPropertySource(properties = ["harbor.name=Tortuga"])
class TheHarborManagementPageShowsTheStockAcceptanceTest {

    @Test
    fun `A new Harbor shows every Cargo of the catalog with a Stock of 3`() {
        // Given a Harbor that has just opened for the first time
        // (the fakes' Starting Stock stands in for the seed of V7__stocks.sql, see ADR-0010)
        fakes.reset()

        // When the User opens the harbor management page
        val stock = stock()

        // Then every Cargo of the catalog is shown with a Stock of 3
        val catalog = SeedData.allCargo().map { it.name }
        stock.map { it["name"] as String } shouldContainExactlyInAnyOrder catalog
        stock.forEach { it["quantity"] shouldBe 3 }
        stock.forEach { it["cargoId"] shouldBe SeedData.cargoIdOf(it["name"] as String).toString() }
    }

    @Test
    fun `Cargo with no Stock is shown with a Stock of 0`() {
        // Given the Stock holds no Rum
        fakes.givenStockOf("Rum", 0)

        // When the User opens the harbor management page
        val stock = stock()

        // Then Rum is shown with a Stock of 0
        stock.single { it["name"] == "Rum" }["quantity"] shouldBe 0
        // And the rest of the catalog is still shown
        stock.size shouldBe SeedData.allCargo().size
        stock.filter { it["name"] != "Rum" }.forEach { it["quantity"] shouldBe 3 }
    }

    @Test
    fun `Loading Cargo lowers the Stock shown`() {
        // Given the Stock holds 3 Rum
        fakes.givenStockOf("Rum", 3)
        // And a ship with a new Shipping
        val shipId = restTemplate.aShipBeingPrepared()

        // When the User loads Rum onto that ship
        val load = restTemplate.postForEntity(
            "/web/ships/$shipId/cargos", mapOf("cargoId" to SeedData.cargoIdOf("Rum")), Map::class.java
        )
        load.statusCode shouldBe HttpStatus.OK

        // And the User opens the harbor management page
        val stock = stock()

        // Then Rum is shown with a Stock of 2
        stock.single { it["name"] == "Rum" }["quantity"] shouldBe 2
        stock.filter { it["name"] != "Rum" }.forEach { it["quantity"] shouldBe 3 }
    }

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var fakes: FakeDrivenPorts

    @BeforeEach
    fun resetHarbor() {
        fakes.reset()
    }

    fun stock(): List<Map<*, *>> {
        val response = restTemplate.getForEntity("/web/stock", String::class.java)
        response.statusCode shouldBe HttpStatus.OK
        return ObjectMapper().readValue(response.body!!, List::class.java).map { it as Map<*, *> }
    }
}
