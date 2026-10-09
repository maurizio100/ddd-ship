package com.sonicdevelopment.application

import com.sonicdevelopment.application.acceptance.fixtures.STARTING_STOCK
import com.sonicdevelopment.application.acceptance.fixtures.SeedData
import com.sonicdevelopment.application.acceptance.fixtures.aShipBeingPrepared
import com.sonicdevelopment.application.acceptance.fixtures.givenKnownHarbors
import com.sonicdevelopment.application.acceptance.fixtures.resetStockToStartingStock
import com.sonicdevelopment.application.acceptance.fixtures.truncateMutableTables
import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.values.CargoId
import com.sonicdevelopment.domain.model.values.CatainId
import com.sonicdevelopment.domain.model.values.EventId
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.model.values.ShippingId
import com.sonicdevelopment.domain.ports.driven.ShippingOutboxRepository
import com.sonicdevelopment.domain.ports.driven.StockRepositoryPort
import com.sonicdevelopment.domain.ports.driving.shipping.ArrivalManagementPort
import com.sonicdevelopment.domain.ports.driving.shipping.ShippingManagementPort
import com.sonicdevelopment.domain.ports.driving.shipping.ShippingPublishedDTO
import com.sonicdevelopment.driven.adapter.persistence.outbox.ShippingOutboxRepositoryAdapter
import com.sonicdevelopment.driven.adapter.persistence.stock.StockRepositoryAdapter
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource
import java.util.UUID

/**
 * A state change and the outbox or inbox rows that belong to it are written in one transaction
 * (ADR-0002, ADR-0004). The in-memory fakes of the acceptance tests cannot roll back, so this guarantee is
 * pinned here against the real database: a later write in the same transaction is made to fail, and nothing
 * of the change may remain.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(PostgresTestcontainer::class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(
    properties = [
        "harbor.name=Port Royal",
        "spring.kafka.bootstrap-servers=localhost:1",
        "spring.kafka.listener.auto-startup=false",
    ]
)
@DbTest
class FailedChangeLeavesNoTraceTest {

    /** The real outbox, except that [broadcastShipping] fails while [armed]. */
    class FailingShippingOutbox(private val real: ShippingOutboxRepositoryAdapter) : ShippingOutboxRepository by real {
        var armed = false

        override fun broadcastShipping(ship: Ship, originHarbor: HarborName) {
            check(!armed) { "The outbox write fails" }
            real.broadcastShipping(ship, originHarbor)
        }
    }

    /** The real Stock, except that [putIntoStock] fails once [succeedingPuts] puts went through while [armed]. */
    class FailingStock(private val real: StockRepositoryAdapter) : StockRepositoryPort by real {
        var armed = false
        var succeedingPuts = 0
        var putsThrough = 0

        override fun putIntoStock(cargoId: CargoId, quantity: Int) {
            if (armed && putsThrough >= succeedingPuts) throw IllegalStateException("The Stock update fails")
            real.putIntoStock(cargoId, quantity)
            putsThrough++
        }
    }

    @TestConfiguration
    class FailingDrivenPorts {
        @Bean
        @Primary
        fun failingShippingOutbox(real: ShippingOutboxRepositoryAdapter) = FailingShippingOutbox(real)

        @Bean
        @Primary
        fun failingStock(real: StockRepositoryAdapter) = FailingStock(real)
    }

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    lateinit var shippingManagementPort: ShippingManagementPort

    @Autowired
    lateinit var arrivalManagementPort: ArrivalManagementPort

    @Autowired
    lateinit var failingOutbox: FailingShippingOutbox

    @Autowired
    lateinit var failingStock: FailingStock

    @BeforeEach
    fun everythingWorksAgain() {
        jdbcTemplate.truncateMutableTables()
        jdbcTemplate.resetStockToStartingStock()
        failingOutbox.armed = false
        failingStock.armed = false
        failingStock.succeedingPuts = 0
        failingStock.putsThrough = 0
    }

    @Test
    fun `A Release whose outbox write fails leaves no Shipping change and no outbox row`() {
        val shipId = restTemplate.aShipBeingPrepared()
        jdbcTemplate.givenKnownHarbors("Tortuga")
        val before = shippingRowOf(shipId)
        before["shipping_state"] shouldBe "PREPARING"
        before["destination_harbor"] shouldBe null
        failingOutbox.armed = true

        shouldThrow<IllegalStateException> {
            shippingManagementPort.releaseShipping(ShipId(shipId), HarborName("Tortuga"))
        }

        shippingRowOf(shipId) shouldBe before
        count("shipping_outbox") shouldBe 0
    }

    @Test
    fun `An Arrival whose Stock update fails leaves no inbox row, Arrival, fleet, Stock or outbox change`() {
        val eventId = UUID.randomUUID()
        val arrival = ShippingPublishedDTO(
            shipId = ShipId(UUID.randomUUID()),
            shipName = "Flying Dutchman",
            catainId = CatainId(SeedData.aCatainId),
            shippingId = ShippingId(UUID.randomUUID()),
            cargoIds = listOf(CargoId(SeedData.cargoIdOf("Rum")), CargoId(SeedData.cargoIdOf("Ale"))),
            originHarbor = HarborName("Tortuga"),
            destinationHarbor = HarborName("Port Royal"),
        )
        failingStock.armed = true
        failingStock.succeedingPuts = 1

        shouldThrow<IllegalStateException> {
            arrivalManagementPort.receiveShippingPublished(EventId(eventId), arrival)
        }

        // the first put really reached the database before the second one failed
        failingStock.putsThrough shouldBe 1
        jdbcTemplate.queryForObject(
            "SELECT count(*) FROM inbox_events WHERE event_id = ?", Int::class.java, eventId
        ) shouldBe 0
        jdbcTemplate.queryForObject(
            "SELECT count(*) FROM arrivals WHERE shipping_id = ?", Int::class.java, arrival.shippingId.id
        ) shouldBe 0
        count("ships") shouldBe 0
        availableShips().shouldBeEmpty()
        jdbcTemplate.queryForList("SELECT stock_quantity FROM stocks", Int::class.java)
            .forEach { it shouldBe STARTING_STOCK }
        count("shipping_outbox") shouldBe 0
    }

    private fun shippingRowOf(shipId: UUID): Map<String, Any?> = jdbcTemplate.queryForMap(
        """
        SELECT s.shipping_state, s.destination_harbor, s.sailors_code
        FROM shippings s JOIN ships sh ON s.ship_id = sh.id
        WHERE sh.ship_id = ?
        """.trimIndent(),
        shipId,
    )

    private fun count(table: String) = jdbcTemplate.queryForObject("SELECT count(*) FROM $table", Int::class.java)

    private fun availableShips(): List<*> =
        restTemplate.getForEntity("/web/ships", List::class.java).body!!
}
