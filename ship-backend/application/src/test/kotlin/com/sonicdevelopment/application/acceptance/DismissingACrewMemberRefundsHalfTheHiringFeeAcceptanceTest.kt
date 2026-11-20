package com.sonicdevelopment.application.acceptance

import com.fasterxml.jackson.databind.ObjectMapper
import com.sonicdevelopment.application.acceptance.fixtures.FakeDrivenPorts
import com.sonicdevelopment.application.acceptance.fixtures.FakeHarborTest
import com.sonicdevelopment.application.acceptance.fixtures.MITTENS_ID
import com.sonicdevelopment.application.acceptance.fixtures.SeedData
import com.sonicdevelopment.application.acceptance.fixtures.aShipBeingPrepared
import com.sonicdevelopment.application.acceptance.fixtures.aShippingPublishedRecord
import com.sonicdevelopment.application.acceptance.fixtures.givenRecruitPool
import com.sonicdevelopment.application.acceptance.fixtures.givenSavings
import com.sonicdevelopment.domain.model.CrewMember
import com.sonicdevelopment.domain.model.Recruit
import com.sonicdevelopment.domain.model.values.CrewMemberId
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.driving.adapter.messaging.ShippingEventListener
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.TestPropertySource
import java.util.UUID

/**
 * Feature: Dismissing a Crew Member refunds half the Hiring Fee.
 *
 * This Harbor is "Port Royal", on in-memory fakes. The Hiring Fee is 50.00 $, the refund 25.00 $.
 */
@FakeHarborTest
@TestPropertySource(properties = ["harbor.name=Port Royal"])
class DismissingACrewMemberRefundsHalfTheHiringFeeAcceptanceTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var fakes: FakeDrivenPorts

    @Autowired
    lateinit var shippingEventListener: ShippingEventListener

    private lateinit var pool: List<Recruit>
    private lateinit var saltyWhiskerId: UUID

    @BeforeEach
    fun saltyWhiskerIsInPortRoyal() {
        fakes.reset()
        pool = fakes.givenRecruitPool("Port Royal")
        saltyWhiskerId = restTemplate.aShipBeingPrepared("Salty Whisker")
        fakes.givenSavings("1000.00")
    }

    @Test
    fun `Dismissing a Crew Member refunds half the Hiring Fee into the Savings`() {
        hire(saltyWhiskerId, MITTENS_ID).statusCode shouldBe HttpStatus.OK
        savings() shouldBe "950.00"

        val response = dismiss(saltyWhiskerId, MITTENS_ID)

        response.statusCode shouldBe HttpStatus.OK
        response.body!!["refund"] shouldBe "25.00"
        response.body!!["savings"] shouldBe "975.00"
        crewOf(saltyWhiskerId).shouldBeEmpty()
        savings() shouldBe "975.00"
    }

    @Test
    fun `A refused dismissal refunds nothing`() {
        fakes.givenSavings("950.00")
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
        response.body!!["detail"] shouldBe "Port Royal is not the Home Harbor of Sea Biscuit"
        crewOf(seaBiscuitId) shouldHaveSize 1
        savings() shouldBe "950.00"
    }

    @Test
    fun `Hiring and dismissing a Recruit costs half the Hiring Fee`() {
        fakes.givenSavings("950.00")
        val juniper = pool[1].id.id

        hire(saltyWhiskerId, juniper).statusCode shouldBe HttpStatus.OK
        savings() shouldBe "900.00"
        dismiss(saltyWhiskerId, juniper).statusCode shouldBe HttpStatus.OK

        crewOf(saltyWhiskerId).shouldBeEmpty()
        savings() shouldBe "925.00"
    }

    @Test
    fun `Each Recruit in the Recruit Pool shows the Hiring Fee`() {
        val recruits = getList("/web/recruits")

        recruits.shouldHaveSize(pool.size)
        recruits.forEach { it["hiringFee"] shouldBe "50.00" }
    }

    @Test
    fun `A Crew Member shows the refund it would bring`() {
        hire(saltyWhiskerId, MITTENS_ID).statusCode shouldBe HttpStatus.OK

        val mittens = crewOf(saltyWhiskerId).single { it["id"] == MITTENS_ID.toString() }

        mittens["refund"] shouldBe "25.00"
    }

    private fun dismiss(shipId: UUID, crewMemberId: UUID): ResponseEntity<Map<*, *>> =
        restTemplate.exchange("/web/ships/$shipId/crew/$crewMemberId", HttpMethod.DELETE, null, Map::class.java)

    private fun hire(shipId: UUID, recruitId: UUID): ResponseEntity<Map<*, *>> =
        restTemplate.postForEntity("/web/ships/$shipId/crew", mapOf("recruitId" to recruitId), Map::class.java)

    private fun crewOf(shipId: UUID): List<Map<*, *>> =
        (getList("/web/ships").single { it["id"] == shipId.toString() }["crew"] as List<*>).map { it as Map<*, *> }

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
