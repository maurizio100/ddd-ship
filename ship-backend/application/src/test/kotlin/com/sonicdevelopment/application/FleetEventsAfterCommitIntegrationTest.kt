package com.sonicdevelopment.application

import com.sonicdevelopment.application.acceptance.fixtures.FleetEventStream
import com.sonicdevelopment.application.acceptance.fixtures.resetStockToStartingStock
import com.sonicdevelopment.application.acceptance.fixtures.truncateMutableTables
import com.sonicdevelopment.domain.model.values.CatainId
import com.sonicdevelopment.domain.model.values.EventId
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.model.values.ShippingId
import com.sonicdevelopment.domain.ports.driving.shipping.ArrivalManagementPort
import com.sonicdevelopment.domain.ports.driving.shipping.ShippingPublishedDTO
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.support.TransactionTemplate
import java.time.Duration
import java.util.UUID

/**
 * The contract of the fleet-events push (ADR-0006) that no Gherkin scenario states: an Arrival handled inside
 * a transaction tells the User nothing if it rolls back, and tells the User only once it has committed.
 * The Arrival is handled through the domain port inside a transaction the test controls, because over HTTP
 * and Kafka the transaction boundary cannot be held open.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(PostgresTestcontainer::class, KafkaTestcontainer::class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(properties = ["harbor.name=Port Royal"])
class FleetEventsAfterCommitIntegrationTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    lateinit var transactionTemplate: TransactionTemplate

    @Autowired
    lateinit var arrivalManagementPort: ArrivalManagementPort

    @LocalServerPort
    var port: Int = 0

    lateinit var fleet: FleetEventStream

    @BeforeEach
    fun aUserIsLookingAtTheFleet() {
        jdbcTemplate.truncateMutableTables()
        jdbcTemplate.resetStockToStartingStock()
        fleet = FleetEventStream.open("http://localhost:$port")
    }

    @AfterEach
    fun theUserLooksAway() {
        fleet.close()
    }

    @Test
    fun `Nothing is pushed for an Arrival that is rolled back, and an Arrival is pushed only once committed`() {
        // An Arrival handled inside a transaction that rolls back tells the User nothing
        val rolledBack = anArrivalAtPortRoyal("Flying Dutchman")
        transactionTemplate.executeWithoutResult {
            arrivalManagementPort.receiveShippingPublished(EventId(UUID.randomUUID()), rolledBack)
            it.setRollbackOnly()
        }
        availableShips().shouldBeEmpty()

        // An Arrival is pushed only once its transaction commits
        val committed = anArrivalAtPortRoyal("Black Pearl")
        transactionTemplate.executeWithoutResult {
            arrivalManagementPort.receiveShippingPublished(EventId(UUID.randomUUID()), committed)
            fleet.next(Duration.ofMillis(500)) shouldBe null
        }

        val pushed = fleet.next().shouldNotBeNull()
        pushed.name shouldBe "ship-arrived"
        pushed.data["shipId"] shouldBe committed.shipId.id.toString()
        fleet.next(Duration.ofMillis(500)) shouldBe null
    }

    private fun availableShips(): List<Map<*, *>> {
        val response = restTemplate.getForEntity("/web/ships", List::class.java)
        response.statusCode shouldBe HttpStatus.OK
        return response.body!!.map { it as Map<*, *> }
    }

    private fun anArrivalAtPortRoyal(shipName: String) = ShippingPublishedDTO(
        shipId = ShipId(UUID.randomUUID()),
        shipName = shipName,
        catainId = CatainId(
            jdbcTemplate.queryForObject("SELECT catain_id FROM catains ORDER BY id LIMIT 1", UUID::class.java)!!
        ),
        shippingId = ShippingId(UUID.randomUUID()),
        cargoIds = emptyList(),
        originHarbor = HarborName("Tortuga"),
        destinationHarbor = HarborName("Port Royal"),
    )
}
