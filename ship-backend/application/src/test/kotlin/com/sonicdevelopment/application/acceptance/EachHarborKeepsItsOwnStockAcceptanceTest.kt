package com.sonicdevelopment.application.acceptance

import com.sonicdevelopment.application.acceptance.fixtures.FakeDrivenPorts
import com.sonicdevelopment.application.acceptance.fixtures.FakeHarborTest
import com.sonicdevelopment.application.acceptance.fixtures.SeedData
import com.sonicdevelopment.application.acceptance.fixtures.aShipBeingPrepared
import com.sonicdevelopment.application.acceptance.fixtures.givenStockOf
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import java.util.*

/**
 * Feature: Each Harbor keeps its own Stock.
 *
 * The Stock is observed the way the User sees it: through the Available Cargo (`GET /web/cargos`).
 */
@FakeHarborTest
@TestPropertySource(properties = ["harbor.name=Tortuga"])
class EachHarborKeepsItsOwnStockAcceptanceTest {

    @Test
    fun `Loading Cargo takes it out of the Stock`() {
        // Given the Stock of "Tortuga" holds 2 "Rum"
        fakes.givenStockOf("Rum", 2)
        // And a ship at "Tortuga" is being prepared
        val shipId = restTemplate.aShipBeingPrepared()

        // When the User loads "Rum" onto the ship
        val load = load(shipId, "Rum")

        // Then the Stock of "Tortuga" holds 1 "Rum"
        load.statusCode shouldBe HttpStatus.OK
        stockOf("Rum") shouldBe 1
    }

    @Test
    fun `Unloading Cargo while preparing puts it back into the Stock`() {
        // Given the Stock of "Tortuga" holds 1 "Rum"
        // And a ship at "Tortuga" being prepared has "Rum" loaded
        fakes.givenStockOf("Rum", 2)
        val shipId = restTemplate.aShipBeingPrepared()
        load(shipId, "Rum").statusCode shouldBe HttpStatus.OK
        stockOf("Rum") shouldBe 1

        // When the User unloads "Rum" from the ship
        val unload = unload(shipId, "Rum")

        // Then the Stock of "Tortuga" holds 2 "Rum"
        unload.statusCode shouldBe HttpStatus.OK
        stockOf("Rum") shouldBe 2
    }

    @Test
    fun `Cargo that is out of Stock cannot be loaded`() {
        // Given the Stock of "Tortuga" holds no "Silk"
        fakes.givenStockOf("Silk", 0)

        // Then "Silk" is not among the Available Cargo at "Tortuga"
        availableCargo().map { it["name"] } shouldNotContain "Silk"
        // And loading "Silk" onto a ship at "Tortuga" is rejected
        val shipId = restTemplate.aShipBeingPrepared()
        val load = load(shipId, "Silk")
        load.statusCode shouldBe HttpStatus.CONFLICT
        load.headers.contentType?.isCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON) shouldBe true
        load.body!!["detail"] shouldBe "Silk is out of Stock"
        loadedCargo(shipId) shouldNotContain "Silk"
    }

    @Test
    fun `A rejected load leaves the Stock unchanged`() {
        // Given the Stock of "Tortuga" holds 2 "Rum"
        fakes.givenStockOf("Rum", 2)
        // And a ship at "Tortuga" being prepared has a Current Weight of 14.0
        val shipId = restTemplate.aShipBeingPrepared()
        listOf("Tobacco", "Silk", "Ale", "Wheat", "Planks", "Paprika").forEach {
            load(shipId, it).statusCode shouldBe HttpStatus.OK
        }
        currentWeight(shipId) shouldBe 14.0

        // When the User loads "Rum" onto the ship
        val load = load(shipId, "Rum")

        // Then the load is rejected because the ship would exceed its Max Weight
        load.statusCode shouldBe HttpStatus.CONFLICT
        (load.body!!["detail"] as String) shouldContain "Max Weight"
        // And the Stock of "Tortuga" still holds 2 "Rum"
        stockOf("Rum") shouldBe 2
        loadedCargo(shipId) shouldContainExactlyInAnyOrder
            listOf("Tobacco", "Silk", "Ale", "Wheat", "Planks", "Paprika")
    }

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var fakes: FakeDrivenPorts

    @BeforeEach
    fun resetHarbor() {
        fakes.reset()
    }

    fun load(shipId: UUID, cargoName: String) =
        restTemplate.postForEntity(
            "/web/ships/$shipId/cargos", mapOf("cargoId" to SeedData.cargoIdOf(cargoName)), Map::class.java
        )

    fun unload(shipId: UUID, cargoName: String) =
        restTemplate.exchange(
            "/web/ships/$shipId/cargos/${SeedData.cargoIdOf(cargoName)}", HttpMethod.DELETE, null, Map::class.java
        )

    fun availableCargo(): List<Map<*, *>> {
        val response = restTemplate.getForEntity("/web/cargos", List::class.java)
        response.statusCode shouldBe HttpStatus.OK
        return response.body!!.map { it as Map<*, *> }
    }

    /** The Stock of [cargoName] as the User sees it among the Available Cargo; 0 when it isn't offered. */
    fun stockOf(cargoName: String): Int =
        (availableCargo().singleOrNull { it["name"] == cargoName }?.get("stock") as Int?) ?: 0

    fun ship(shipId: UUID): Map<*, *> {
        val response = restTemplate.getForEntity("/web/ships/$shipId", Map::class.java)
        response.statusCode shouldBe HttpStatus.OK
        return response.body!!
    }

    fun loadedCargo(shipId: UUID): List<String> =
        (ship(shipId)["cargo"] as List<*>).map { (it as Map<*, *>)["name"] as String }

    fun currentWeight(shipId: UUID): Double = (ship(shipId)["weight"] as Number).toDouble()
}
