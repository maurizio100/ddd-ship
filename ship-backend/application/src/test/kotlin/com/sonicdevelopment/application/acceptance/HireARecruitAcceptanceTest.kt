package com.sonicdevelopment.application.acceptance

import com.fasterxml.jackson.databind.ObjectMapper
import com.sonicdevelopment.application.acceptance.fixtures.FakeDrivenPorts
import com.sonicdevelopment.application.acceptance.fixtures.FakeHarborTest
import com.sonicdevelopment.application.acceptance.fixtures.MITTENS_ID
import com.sonicdevelopment.application.acceptance.fixtures.SeedData
import com.sonicdevelopment.application.acceptance.fixtures.aShipBeingPrepared
import com.sonicdevelopment.application.acceptance.fixtures.aShippingPublishedRecord
import com.sonicdevelopment.application.acceptance.fixtures.givenKnownHarbors
import com.sonicdevelopment.application.acceptance.fixtures.givenRecruitPool
import com.sonicdevelopment.application.acceptance.fixtures.givenSavings
import com.sonicdevelopment.application.acceptance.fixtures.release
import com.sonicdevelopment.domain.model.Recruit
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.service.RecruitPoolGenerator
import com.sonicdevelopment.driving.adapter.messaging.ShippingEventListener
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.TestPropertySource
import java.util.UUID

/**
 * Feature: Hire a Recruit.
 *
 * This Harbor is "Port Royal", on in-memory fakes. Its Recruit Pool offers "Mittens" (trait "Strong Paws") and four
 * generated Recruits. The Hire is `POST /web/ships/{id}/crew`; state is read over HTTP: `GET /web/ships`,
 * `/web/savings` and `/web/recruits`. The Hiring Fee is 50.00 $ and a Sloop carries 2 Crew Members.
 */
@FakeHarborTest
@TestPropertySource(properties = ["harbor.name=Port Royal"])
class HireARecruitAcceptanceTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var fakes: FakeDrivenPorts

    @Autowired
    lateinit var shippingEventListener: ShippingEventListener

    private lateinit var pool: List<Recruit>
    private lateinit var saltyWhiskerId: UUID

    @BeforeEach
    fun portRoyalOffersMittensAndSaltyWhiskerIsInPort() {
        fakes.reset()
        // Given the Harbor "Port Royal" offers the Recruit "Mittens" with the trait "Strong Paws"
        pool = fakes.givenRecruitPool("Port Royal")
        // And "Salty Whisker" is a Sloop with the Home Harbor "Port Royal", at "Port Royal"
        saltyWhiskerId = restTemplate.aShipBeingPrepared("Salty Whisker")
    }

    @Test
    fun `Hiring a Recruit adds them to the Crew and pays the Hiring Fee`() {
        // Given the Savings of "Port Royal" are 1000.00 $
        fakes.givenSavings("1000.00")

        // When the User Hires "Mittens" onto "Salty Whisker"
        val response = hire(saltyWhiskerId, MITTENS_ID)

        // Then "Mittens" is a Crew Member of "Salty Whisker" with the trait "Strong Paws"
        response.statusCode shouldBe HttpStatus.OK
        val crew = crewOf(saltyWhiskerId)
        crew shouldHaveSize 1
        crew.single()["id"] shouldBe MITTENS_ID.toString()
        crew.single()["name"] shouldBe "Mittens"
        crew.single()["trait"] shouldBe "Strong Paws"
        // And the Savings of "Port Royal" are 950.00 $
        savings() shouldBe "950.00"
    }

    @Test
    fun `A new Recruit takes the place of a Hired one`() {
        // Given the Savings of "Port Royal" are 1000.00 $
        fakes.givenSavings("1000.00")

        // When the User Hires "Mittens" onto "Salty Whisker"
        hire(saltyWhiskerId, MITTENS_ID).statusCode shouldBe HttpStatus.OK

        // Then "Mittens" is no longer offered in the Recruit Pool
        val offered = recruitIds()
        offered.contains(MITTENS_ID.toString()) shouldBe false
        // And the Recruit Pool offers five Recruits, the new one being the next never generated
        offered shouldHaveSize 5
        offered shouldBe (pool.drop(1) + RecruitPoolGenerator(HarborName("Port Royal")).recruit(5)).map { it.id.id.toString() }
    }

    @Test
    fun `A ship with a full Crew is offered no Recruits`() {
        // Given "Salty Whisker" already has 2 Crew Members
        fakes.givenSavings("1000.00")
        fillTheCrew()

        // When the User looks at the Crew of "Salty Whisker"
        val ship = shipOf(saltyWhiskerId)

        // Then the Crew is at its capacity, so the User is offered no Hire (the screen shows the notice)
        (ship["crew"] as List<*>) shouldHaveSize 2
        ship["crewCapacity"] shouldBe 2
    }

    @Test
    fun `A ship with a full Crew cannot Hire`() {
        // Given "Salty Whisker" already has 2 Crew Members
        fakes.givenSavings("1000.00")
        fillTheCrew()
        val savingsBefore = savings()
        val poolBefore = recruitIds()

        // When the User Hires "Mittens" onto "Salty Whisker"
        val response = hire(saltyWhiskerId, MITTENS_ID)

        // Then the Hire is refused because the Crew is full
        response.statusCode shouldBe HttpStatus.CONFLICT
        response.body!!["title"] shouldBe "Hire refused"
        (response.body!!["detail"] as String) shouldContain "Crew is full"
        // And the Savings and the Recruit Pool are unchanged
        savings() shouldBe savingsBefore
        recruitIds() shouldBe poolBefore
        crewOf(saltyWhiskerId) shouldHaveSize 2
    }

    @Test
    fun `A Harbor that cannot pay the Hiring Fee cannot Hire`() {
        // Given the Savings of "Port Royal" are 40.00 $
        fakes.givenSavings("40.00")

        // When the User Hires "Mittens" onto "Salty Whisker"
        val response = hire(saltyWhiskerId, MITTENS_ID)

        // Then the Hire is refused because the Savings fall short
        response.statusCode shouldBe HttpStatus.CONFLICT
        response.body!!["title"] shouldBe "Savings do not cover"
        response.body!!["detail"] shouldBe "The Savings do not cover the Hiring Fee of 50.00 $"
        // And "Salty Whisker" has no Crew, the Savings are kept and Mittens is still offered
        crewOf(saltyWhiskerId).shouldBeEmpty()
        savings() shouldBe "40.00"
        recruitIds().contains(MITTENS_ID.toString()) shouldBe true
    }

    @Test
    fun `A ship cannot Hire away from its Home Harbor`() {
        // Given "Sea Biscuit" has the Home Harbor "Tortuga" and is at "Port Royal"
        fakes.givenSavings("1000.00")
        val seaBiscuitId = UUID.randomUUID()
        shippingEventListener.onShippingEvent(
            aShippingPublishedRecord(
                shipId = seaBiscuitId,
                shipName = "Sea Biscuit",
                catainId = SeedData.aCatainId,
                originHarbor = "Tortuga",
                destinationHarbor = "Port Royal",
                homeHarbor = "Tortuga",
            )
        )

        // When the User Hires "Mittens" onto "Sea Biscuit"
        val response = hire(seaBiscuitId, MITTENS_ID)

        // Then the Hire is refused because "Port Royal" is not its Home Harbor
        response.statusCode shouldBe HttpStatus.CONFLICT
        response.body!!["title"] shouldBe "Hire refused"
        response.body!!["detail"] shouldBe "Port Royal is not the Home Harbor of Sea Biscuit"
        savings() shouldBe "1000.00"
        crewOf(seaBiscuitId).shouldBeEmpty()
    }

    @Test
    fun `A ship at sea cannot Hire`() {
        // Given "Old Barnacle" has the Home Harbor "Port Royal" and is at sea
        fakes.givenSavings("1000.00")
        fakes.givenKnownHarbors("Tortuga")
        val oldBarnacleId = restTemplate.aShipBeingPrepared("Old Barnacle")
        restTemplate.release(oldBarnacleId, "Tortuga").statusCode shouldBe HttpStatus.OK
        shipOf(oldBarnacleId)["shippingState"] shouldBe "SHIPPING"

        // When the User Hires "Mittens" onto "Old Barnacle"
        val response = hire(oldBarnacleId, MITTENS_ID)

        // Then the Hire is refused because "Old Barnacle" is at sea
        response.statusCode shouldBe HttpStatus.CONFLICT
        response.body!!["title"] shouldBe "Hire refused"
        response.body!!["detail"] shouldBe "Old Barnacle is at sea"
        savings() shouldBe "1000.00"
        crewOf(oldBarnacleId).shouldBeEmpty()
    }

    /** Two Hires of generated Recruits fill the Crew of "Salty Whisker"; "Mittens" stays offered. */
    private fun fillTheCrew() {
        hire(saltyWhiskerId, pool[1].id.id).statusCode shouldBe HttpStatus.OK
        hire(saltyWhiskerId, pool[2].id.id).statusCode shouldBe HttpStatus.OK
    }

    private fun hire(shipId: UUID, recruitId: UUID): ResponseEntity<Map<*, *>> =
        restTemplate.postForEntity("/web/ships/$shipId/crew", mapOf("recruitId" to recruitId), Map::class.java)

    private fun shipOf(shipId: UUID): Map<*, *> =
        getList("/web/ships").single { it["id"] == shipId.toString() }

    private fun crewOf(shipId: UUID): List<Map<*, *>> =
        (shipOf(shipId)["crew"] as List<*>).map { it as Map<*, *> }

    private fun recruitIds(): List<String> = getList("/web/recruits").map { it["id"] as String }

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
