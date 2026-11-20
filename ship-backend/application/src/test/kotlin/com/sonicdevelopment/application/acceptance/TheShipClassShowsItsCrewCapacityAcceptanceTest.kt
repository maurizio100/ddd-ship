package com.sonicdevelopment.application.acceptance

import com.fasterxml.jackson.databind.ObjectMapper
import com.sonicdevelopment.application.acceptance.fixtures.FakeDrivenPorts
import com.sonicdevelopment.application.acceptance.fixtures.FakeHarborTest
import com.sonicdevelopment.application.acceptance.fixtures.SeedData
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestPropertySource

/** Feature: The Ship Class shows its Crew Capacity when a ship is created. One Harbor ("Port Royal") on fakes. */
@FakeHarborTest
@TestPropertySource(properties = ["harbor.name=Port Royal"])
class TheShipClassShowsItsCrewCapacityAcceptanceTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var fakes: FakeDrivenPorts

    @BeforeEach
    fun aFreshHarbor() {
        fakes.reset()
    }

    @ParameterizedTest(name = "{0} is offered with the Crew Capacity {1}")
    @CsvSource("SLOOP,2", "BRIG,4", "GALLEON,6")
    fun `Each Ship Class is offered with its Crew Capacity`(shipClass: String, capacity: Int) {
        offers().single { it["shipClass"] == shipClass }["crewCapacity"] shouldBe capacity
    }

    @Test
    fun `The offered Crew Capacity is the one the ship gets`() {
        val offered = offers().single { it["shipClass"] == "BRIG" }["crewCapacity"]
        offered shouldBe 4

        val created = restTemplate.postForEntity(
            "/web/ships",
            mapOf("name" to "Salty Whisker", "catainId" to SeedData.aCatainId, "shipClass" to "BRIG"),
            Map::class.java,
        )
        created.statusCode shouldBe HttpStatus.OK
        val id = created.body!!["id"]

        val details = restTemplate.getForEntity("/web/ships/$id", Map::class.java)
        details.body!!["crewCapacity"] shouldBe offered
    }

    private fun offers(): List<Map<*, *>> {
        val response = restTemplate.getForEntity("/web/ship-classes", String::class.java)
        response.statusCode shouldBe HttpStatus.OK
        return ObjectMapper().readValue(response.body!!, List::class.java).map { it as Map<*, *> }
    }
}
