package com.sonicdevelopment.application.acceptance

import com.fasterxml.jackson.databind.ObjectMapper
import com.sonicdevelopment.application.acceptance.fixtures.FakeDrivenPorts
import com.sonicdevelopment.application.acceptance.fixtures.FakeHarborTest
import com.sonicdevelopment.application.acceptance.fixtures.SeedData
import com.sonicdevelopment.application.acceptance.fixtures.aShippingPublishedRecord
import com.sonicdevelopment.application.acceptance.fixtures.givenKnownHarbors
import com.sonicdevelopment.application.acceptance.fixtures.givenPriceOf
import com.sonicdevelopment.application.acceptance.fixtures.release
import com.sonicdevelopment.driving.adapter.messaging.ShippingEventListener
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestPropertySource
import java.util.*

/**
 * Feature: A Home Harbor refuses its own ship.
 *
 * The Harbor is "Port Royal", on in-memory fakes. "Salty Whisker", whose Home Harbor is "Port Royal", arrives
 * from "Tortuga" with 2 Rum aboard through the [ShippingEventListener]. The refusal is
 * `POST /web/incoming-ships/{id}/refusal`; state is read over HTTP and from the fake outbox.
 */
@FakeHarborTest
@TestPropertySource(properties = ["harbor.name=Port Royal"])
class AHomeHarborRefusesItsOwnShipAcceptanceTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var fakes: FakeDrivenPorts

    @Autowired
    lateinit var shippingEventListener: ShippingEventListener

    private val saltyWhiskerId: UUID = UUID.randomUUID()
    private val rum = SeedData.cargoIdOf("Rum")

    /** Background: the Harbor is "Port Royal"; "Salty Whisker", Home Harbor "Port Royal", has arrived with 2 Rum. */
    @BeforeEach
    fun saltyWhiskerHasArrived() {
        fakes.reset()
        fakes.givenPriceOf("Rum", "40.00")
        shippingEventListener.onShippingEvent(
            aShippingPublishedRecord(
                shipId = saltyWhiskerId,
                shipName = "Salty Whisker",
                catainId = SeedData.aCatainId,
                shippingId = UUID.randomUUID(),
                cargoIds = listOf(rum, rum),
                originHarbor = "Tortuga",
                destinationHarbor = "Port Royal",
                homeHarbor = "Port Royal",
            )
        )
    }

    @Test
    fun `A Home Harbor refusing its own ship keeps it in the fleet with its Cargo aboard`() {
        // Given the Stock and the Savings of "Port Royal"
        val stockBefore = stock()
        val savingsBefore = savings()

        // When the User refuses "Salty Whisker"
        refuse().statusCode shouldBe HttpStatus.NO_CONTENT

        // Then "Salty Whisker" stays in the fleet of "Port Royal" with its 2 Rum aboard
        val entry = ships().single { it["id"] == saltyWhiskerId.toString() }
        entry["shippingState"] shouldBe "IDLE"
        cargoAboard() shouldBe listOf("Rum", "Rum")
        fakes.outbox.shippingPublished() shouldBe emptyList()
        // And "Salty Whisker" is no longer an Incoming Ship
        entry["incoming"] shouldBe false
        incomingShips().none { it["shipId"] == saltyWhiskerId.toString() } shouldBe true
        // And the Stock and the Savings of "Port Royal" are unchanged
        stock() shouldBe stockBefore
        savings() shouldBe savingsBefore
    }

    @Test
    fun `Cargo refused by the Home Harbor can be delivered to another Harbor`() {
        // Given "Port Royal" refused the 2 Rum aboard its own ship "Salty Whisker"
        refuse().statusCode shouldBe HttpStatus.NO_CONTENT
        fakes.givenKnownHarbors("Tortuga")

        // When the User Releases "Salty Whisker" on a new Shipping to "Tortuga"
        startShipping().statusCode shouldBe HttpStatus.OK
        restTemplate.release(saltyWhiskerId, "Tortuga").statusCode shouldBe HttpStatus.OK

        // Then "Salty Whisker" sails to "Tortuga" with its 2 Rum aboard
        val published = fakes.outbox.shippingPublished().single()
        published.shipId shouldBe saltyWhiskerId
        published.destinationHarbor shouldBe "Tortuga"
        published.cargoIds shouldBe listOf(rum, rum)
        cargoAboard() shouldBe emptyList()
    }

    @Test
    fun `Cargo refused by the Home Harbor cannot be unloaded into its Stock`() {
        // Given "Port Royal" refused the 2 Rum aboard its own ship "Salty Whisker"
        refuse().statusCode shouldBe HttpStatus.NO_CONTENT
        // And the User is preparing a new Shipping for "Salty Whisker"
        startShipping().statusCode shouldBe HttpStatus.OK
        val stockBefore = stock()

        // When the User tries to unload a Rum from "Salty Whisker"
        val unloading = unload("Rum")

        // Then the unloading is refused
        unloading.statusCode shouldBe HttpStatus.CONFLICT
        unloading.body!!["title"] shouldBe "Refused Cargo"
        // And "Salty Whisker" still has its 2 Rum aboard
        cargoAboard() shouldBe listOf("Rum", "Rum")
        // And the Stock of "Port Royal" is unchanged
        stock() shouldBe stockBefore
    }

    @Test
    fun `Cargo loaded at the Home Harbor can still be unloaded while preparing`() {
        // Given "Port Royal" refused the 2 Rum aboard its own ship "Salty Whisker"
        refuse().statusCode shouldBe HttpStatus.NO_CONTENT
        // And the User is preparing a new Shipping for "Salty Whisker" and has loaded 1 Silk
        startShipping().statusCode shouldBe HttpStatus.OK
        val stockBeforeLoading = stock()
        load("Silk").statusCode shouldBe HttpStatus.OK
        stock()["Silk"] shouldBe stockBeforeLoading["Silk"]!! - 1

        // When the User unloads the Silk from "Salty Whisker"
        unload("Silk").statusCode shouldBe HttpStatus.OK

        // Then the Silk goes back into the Stock of "Port Royal"
        stock()["Silk"] shouldBe stockBeforeLoading["Silk"]
        // And "Salty Whisker" still has its 2 Rum aboard
        cargoAboard() shouldBe listOf("Rum", "Rum")
    }

    private fun refuse() =
        restTemplate.postForEntity("/web/incoming-ships/$saltyWhiskerId/refusal", null, Map::class.java)

    private fun startShipping() =
        restTemplate.postForEntity("/web/ships/$saltyWhiskerId/shippings", null, Map::class.java)

    private fun load(cargoName: String) =
        restTemplate.postForEntity(
            "/web/ships/$saltyWhiskerId/cargos", mapOf("cargoId" to SeedData.cargoIdOf(cargoName)), Map::class.java
        )

    private fun unload(cargoName: String) =
        restTemplate.exchange(
            "/web/ships/$saltyWhiskerId/cargos/${SeedData.cargoIdOf(cargoName)}",
            HttpMethod.DELETE, null, Map::class.java
        )

    /** The names of the Cargo aboard "Salty Whisker" that is not Loaded Cargo, from the ship detail. */
    private fun cargoAboard(): List<String> {
        val response = restTemplate.getForEntity("/web/ships/$saltyWhiskerId", Map::class.java)
        response.statusCode shouldBe HttpStatus.OK
        val aboard = response.body!!["cargoAboard"] as List<*>? ?: return emptyList<String>().also {
            error("The ship detail has no cargoAboard field")
        }
        return aboard.map { (it as Map<*, *>)["name"] as String }
    }

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
