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
import com.sonicdevelopment.domain.model.CrewMember
import com.sonicdevelopment.domain.model.values.CrewMemberId
import com.sonicdevelopment.domain.model.values.ShipId
import org.springframework.http.HttpMethod
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
 * Feature: Dismiss a Crew Member.
 *
 * This Harbor is "Port Royal", on in-memory fakes. Its Recruit Pool offers "Mittens" (trait "Strong Paws") and four
 * generated Recruits. The Hire is `POST /web/ships/{id}/crew`; state is read over HTTP: `GET /web/ships`,
 * `/web/savings` and `/web/recruits`. The Hiring Fee is 50.00 $ and a Sloop carries 2 Crew Members.
 */
@FakeHarborTest
@TestPropertySource(properties = ["harbor.name=Port Royal"])
class DismissACrewMemberAcceptanceTest {

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
        // And "Salty Whisker" is a Sloop with the Home Harbor "Port Royal", with the Crew Member "Mittens"
        saltyWhiskerId = restTemplate.aShipBeingPrepared("Salty Whisker")
        fakes.givenSavings("1000.00")
        hire(saltyWhiskerId, MITTENS_ID).statusCode shouldBe HttpStatus.OK
    }

    @Test
    fun `Dismissing a Crew Member removes them from the Crew`() {
        val response = dismiss(saltyWhiskerId, MITTENS_ID)

        response.statusCode shouldBe HttpStatus.OK
        crewOf(saltyWhiskerId).shouldBeEmpty()
        savings() shouldBe "975.00"
    }

    @Test
    fun `A dismissed Crew Member does not return to the Recruit Pool`() {
        dismiss(saltyWhiskerId, MITTENS_ID).statusCode shouldBe HttpStatus.OK

        recruitIds().contains(MITTENS_ID.toString()) shouldBe false
    }

    @Test
    fun `A Crew Member cannot be dismissed away from the Home Harbor`() {
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
        fakes.fleet.addCrewMember(ShipId(seaBiscuitId), CrewMember(CrewMemberId(MITTENS_ID), "Mittens", "Strong Paws"))

        val response = dismiss(seaBiscuitId, MITTENS_ID)

        response.statusCode shouldBe HttpStatus.CONFLICT
        response.body!!["title"] shouldBe "Dismissal refused"
        response.body!!["detail"] shouldBe "Port Royal is not the Home Harbor of Sea Biscuit"
        crewOf(seaBiscuitId) shouldHaveSize 1
    }

    @Test
    fun `A Crew Member cannot be dismissed while the ship is at sea`() {
        fakes.givenKnownHarbors("Tortuga")
        restTemplate.release(saltyWhiskerId, "Tortuga").statusCode shouldBe HttpStatus.OK

        val response = dismiss(saltyWhiskerId, MITTENS_ID)

        response.statusCode shouldBe HttpStatus.CONFLICT
        response.body!!["title"] shouldBe "Dismissal refused"
        response.body!!["detail"] shouldBe "Salty Whisker is at sea"
        crewOf(saltyWhiskerId) shouldHaveSize 1
    }

    private fun dismiss(shipId: UUID, crewMemberId: UUID): ResponseEntity<Map<*, *>> =
        restTemplate.exchange("/web/ships/$shipId/crew/$crewMemberId", HttpMethod.DELETE, null, Map::class.java)

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
