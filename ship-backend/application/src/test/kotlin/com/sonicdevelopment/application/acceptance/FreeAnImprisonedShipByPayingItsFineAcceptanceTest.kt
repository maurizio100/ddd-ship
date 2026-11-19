package com.sonicdevelopment.application.acceptance

import com.fasterxml.jackson.databind.ObjectMapper
import com.sonicdevelopment.application.acceptance.fixtures.FakeDrivenPorts
import com.sonicdevelopment.application.acceptance.fixtures.FakeHarborTest
import com.sonicdevelopment.application.acceptance.fixtures.SeedData
import com.sonicdevelopment.application.acceptance.fixtures.aShipImprisonedRecord
import com.sonicdevelopment.application.acceptance.fixtures.aShippingPublishedRecord
import com.sonicdevelopment.application.acceptance.fixtures.givenSavings
import com.sonicdevelopment.driving.adapter.messaging.CoastGuardEventListener
import com.sonicdevelopment.driving.adapter.messaging.ShippingEventListener
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestPropertySource
import java.util.*

/**
 * Feature: Free an Imprisoned Ship by paying its Fine.
 *
 * This Harbor is "Tortuga", the Home Harbor of "Salty Whisker". The Coast Guard's imprisonment is delivered by
 * handing the listener the record Debezium would relay; state is read over HTTP:
 * `GET /web/imprisoned-ships`, `/web/savings` and `/web/ships`. The Fine is 50.00 $.
 */
@FakeHarborTest
@TestPropertySource(properties = ["harbor.name=Tortuga"])
class FreeAnImprisonedShipByPayingItsFineAcceptanceTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var fakes: FakeDrivenPorts

    @Autowired
    lateinit var shippingEventListener: ShippingEventListener

    @Autowired
    lateinit var coastGuardEventListener: CoastGuardEventListener

    private val saltyWhiskerId: UUID = UUID.randomUUID()
    private val seaBiscuitId: UUID = UUID.randomUUID()

    @BeforeEach
    fun aHarborWithAnImprisonedSaltyWhisker() {
        fakes.reset()
        // Given "Salty Whisker", Home Harbor "Tortuga", is an Imprisoned Ship here
        arrive(saltyWhiskerId, "Salty Whisker")
        coastGuardEventListener.onCoastGuardEvent(
            aShipImprisonedRecord(
                shipId = saltyWhiskerId, shipName = "Salty Whisker", holdingHarbor = "Tortuga", homeHarbor = "Tortuga",
            )
        )
    }

    @Test
    fun `The Imprisoned Ships are shown with their Fine`() {
        // When the User looks at the Imprisoned Ships
        val imprisoned = getList("/web/imprisoned-ships")

        // Then "Salty Whisker" is listed with the Fine of 50.00 $
        val listed = imprisoned.single()
        listed["shipId"] shouldBe saltyWhiskerId.toString()
        listed["name"] shouldBe "Salty Whisker"
        listed["homeHarbor"] shouldBe "Tortuga"
        listed["fine"] shouldBe "50.00"
    }

    @ParameterizedTest
    @CsvSource("1000.00,true", "30.00,false")
    fun `The User sees whether the Savings cover the Fine`(savings: String, covers: Boolean) {
        // Given the Savings are <savings> $
        fakes.givenSavings(savings)

        // When the User looks at the Imprisoned Ships
        val listed = getList("/web/imprisoned-ships").single()

        // Then the Savings cover the Fine: <covers>
        listed["fine"] shouldBe "50.00"
        listed["savingsCoverFine"] shouldBe covers
    }

    @Test
    fun `Paying the Fine frees the ship`() {
        // Given the Savings are 1000.00 $
        fakes.givenSavings("1000.00")

        // When the User pays the Fine of "Salty Whisker"
        val response = restTemplate.postForEntity("/web/imprisoned-ships/$saltyWhiskerId/fine-payment", null, Map::class.java)

        // Then the Fine is paid and the Savings are 950.00 $
        response.statusCode shouldBe HttpStatus.OK
        response.body!!["savings"] shouldBe "950.00"
        savings() shouldBe "950.00"
        // And "Salty Whisker" is no longer an Imprisoned Ship
        getList("/web/imprisoned-ships") shouldBe emptyList()
        getList("/web/ships").single { it["id"] == saltyWhiskerId.toString() }["imprisoned"] shouldBe false
        // And "Salty Whisker" can get a new Shipping
        restTemplate.postForEntity("/web/ships/$saltyWhiskerId/shippings", null, Map::class.java)
            .statusCode shouldBe HttpStatus.OK
    }

    @Test
    fun `The ship stays imprisoned when the Savings fall short`() {
        // Given the Savings are 30.00 $
        fakes.givenSavings("30.00")

        // When the User pays the Fine of "Salty Whisker"
        val response = restTemplate.postForEntity("/web/imprisoned-ships/$saltyWhiskerId/fine-payment", null, Map::class.java)

        // Then the User is told that the Savings do not cover the Fine of 50.00 $
        response.statusCode shouldBe HttpStatus.CONFLICT
        response.body!!["detail"] shouldBe "The Savings do not cover the Fine of 50.00 $"
        // And "Salty Whisker" is still an Imprisoned Ship and the Savings are 30.00 $
        getList("/web/imprisoned-ships").single()["shipId"] shouldBe saltyWhiskerId.toString()
        savings() shouldBe "30.00"
    }

    @Test
    fun `A ship that is not imprisoned has no Fine to pay`() {
        // Given "Sea Biscuit" is in the fleet and not imprisoned, and the Savings are 1000.00 $
        arrive(seaBiscuitId, "Sea Biscuit")
        fakes.givenSavings("1000.00")

        // When the User pays the Fine of "Sea Biscuit"
        val response = restTemplate.postForEntity("/web/imprisoned-ships/$seaBiscuitId/fine-payment", null, Map::class.java)

        // Then the User is told that "Sea Biscuit" is not imprisoned
        response.statusCode shouldBe HttpStatus.CONFLICT
        response.body!!["detail"] shouldBe "Sea Biscuit is not imprisoned"
        // And the Savings are 1000.00 $
        savings() shouldBe "1000.00"
    }

    @Test
    fun `The Imprisoned Ships are not offered while there are none`() {
        // Given the Fine of "Salty Whisker" is paid
        restTemplate.postForEntity("/web/imprisoned-ships/$saltyWhiskerId/fine-payment", null, Map::class.java)

        // When the User looks at the Imprisoned Ships
        // Then there are none
        getList("/web/imprisoned-ships") shouldBe emptyList()
    }

    /** The ship [shipId], Home Harbor "Tortuga", arrives here from "Port Royal" with no Cargo aboard. */
    private fun arrive(shipId: UUID, name: String) {
        shippingEventListener.onShippingEvent(
            aShippingPublishedRecord(
                shipId = shipId,
                shipName = name,
                catainId = SeedData.aCatainId,
                cargoIds = emptyList(),
                originHarbor = "Port Royal",
                destinationHarbor = "Tortuga",
                homeHarbor = "Tortuga",
            )
        )
    }

    private fun savings(): String {
        val response = restTemplate.getForEntity("/web/savings", Map::class.java)
        response.statusCode shouldBe HttpStatus.OK
        return response.body!!["amount"] as String
    }

    private fun getList(url: String): List<Map<*, *>> {
        val response = restTemplate.getForEntity(url, String::class.java)
        response.statusCode shouldBe HttpStatus.OK
        return ObjectMapper().readValue(response.body!!, List::class.java).map { it as Map<*, *> }
    }
}
