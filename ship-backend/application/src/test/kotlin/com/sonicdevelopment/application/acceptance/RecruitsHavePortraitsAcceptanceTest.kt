package com.sonicdevelopment.application.acceptance

import com.fasterxml.jackson.databind.ObjectMapper
import com.sonicdevelopment.application.acceptance.fixtures.FakeDrivenPorts
import com.sonicdevelopment.application.acceptance.fixtures.FakeHarborTest
import com.sonicdevelopment.application.acceptance.fixtures.SeedData
import com.sonicdevelopment.application.acceptance.fixtures.aShippingPublishedRecord
import com.sonicdevelopment.application.acceptance.fixtures.givenKnownHarbors
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.Portrait
import com.sonicdevelopment.domain.ports.driving.harbor.HarborManagementPort
import com.sonicdevelopment.domain.service.RecruitPoolGenerator
import com.sonicdevelopment.driving.adapter.messaging.ShippingEventListener
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestPropertySource
import java.util.UUID

/**
 * Feature: Recruits have portraits.
 *
 * This Harbor is "Tortuga", on in-memory fakes. A Recruit Portrait is derived from the cat's UUID alone, so it is
 * the same at every Harbor. State is read over HTTP.
 */
@FakeHarborTest
@TestPropertySource(properties = ["harbor.name=Tortuga"])
class RecruitsHavePortraitsAcceptanceTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var fakes: FakeDrivenPorts

    @Autowired
    lateinit var harbor: HarborManagementPort

    @Autowired
    lateinit var shippingEventListener: ShippingEventListener

    @BeforeEach
    fun aFreshHarbor() {
        fakes.reset()
        fakes.givenKnownHarbors("Port Royal")
    }

    @Test
    fun `GET web recruits returns a portrait for every Recruit`() {
        harbor.openHarbor()

        val pool = getList("/web/recruits")

        pool.size shouldBe 5
        pool.forEach { it["portrait"] shouldBe portraitJson(UUID.fromString(it["id"] as String)) }
    }

    @Test
    fun `a freshly opened Harbor offers five Recruits, each with a complete portrait`() {
        harbor.openHarbor()

        val pool = getList("/web/recruits")

        pool.size shouldBe 5
        pool.forEach {
            val portrait = it["portrait"] as Map<*, *>
            portrait.keys shouldBe setOf("fur", "coat", "eyes", "accessory")
            portrait.values.forEach { v -> (v as String).isNotBlank() shouldBe true }
        }
    }

    @Test
    fun `Mittens arrives at Tortuga with the portrait she had at Salty Whisker's Home Harbor`() {
        // Given "Mittens" is the first Recruit of "Port Royal", whose portrait is known there
        val mittens = RecruitPoolGenerator(HarborName("Port Royal")).recruit(0)
        val shipId = UUID.randomUUID()

        // When "Salty Whisker" arrives at "Tortuga"
        shippingEventListener.onShippingEvent(
            aShippingPublishedRecord(
                shipId = shipId, shipName = "Salty Whisker", catainId = SeedData.aCatainId,
                originHarbor = "Port Royal", destinationHarbor = "Tortuga", homeHarbor = "Port Royal",
                crew = listOf(Triple(mittens.id.id, mittens.name, mittens.trait)),
            )
        )

        // Then "Mittens" is shown with the same portrait
        val ship = getList("/web/ships").single { it["id"] == shipId.toString() }
        val member = (ship["crew"] as List<*>).single() as Map<*, *>
        member["portrait"] shouldBe portraitJson(mittens.id.id)
        portraitJson(mittens.id.id) shouldBe portraitJson(mittens.portrait)
    }

    private fun portraitJson(id: UUID) = portraitJson(Portrait.of(id))

    private fun portraitJson(p: Portrait) = mapOf(
        "fur" to kebab(p.fur), "coat" to kebab(p.coat), "eyes" to kebab(p.eyes), "accessory" to kebab(p.accessory),
    )

    private fun kebab(e: Enum<*>) = e.name.lowercase().replace('_', '-')

    private fun getList(url: String): List<Map<*, *>> {
        val response = restTemplate.getForEntity(url, String::class.java)
        response.statusCode shouldBe HttpStatus.OK
        return ObjectMapper().readValue(response.body!!, List::class.java).map { it as Map<*, *> }
    }
}
