package com.sonicdevelopment.application

import com.sonicdevelopment.application.acceptance.fixtures.STARTING_SAVINGS
import com.sonicdevelopment.application.acceptance.fixtures.STARTING_STOCK
import com.sonicdevelopment.application.acceptance.fixtures.SeedData
import com.sonicdevelopment.application.acceptance.fixtures.aShipBeingPrepared
import com.sonicdevelopment.application.acceptance.fixtures.givenKnownHarbors
import com.sonicdevelopment.application.acceptance.fixtures.resetSavingsToStartingSavings
import com.sonicdevelopment.application.acceptance.fixtures.resetStockToStartingStock
import com.sonicdevelopment.application.acceptance.fixtures.truncateMutableTables
import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.values.CargoId
import com.sonicdevelopment.domain.model.values.CatainId
import com.sonicdevelopment.domain.model.values.EventId
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.model.values.ShippingId
import com.sonicdevelopment.domain.ports.driven.HarborOutboxRepositoryPort
import com.sonicdevelopment.domain.ports.driven.SavingsRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShippingOutboxRepository
import com.sonicdevelopment.domain.ports.driven.StockRepositoryPort
import com.sonicdevelopment.domain.ports.driving.shipping.ArrivalManagementPort
import com.sonicdevelopment.domain.ports.driving.shipping.ShippingManagementPort
import com.sonicdevelopment.domain.ports.driving.shipping.ShippingPublishedDTO
import com.sonicdevelopment.domain.ports.driving.harbor.HarborManagementPort
import com.sonicdevelopment.domain.ports.driving.harbor.IncomingShipManagementPort
import com.sonicdevelopment.domain.ports.driving.market.MarketPort
import com.sonicdevelopment.driven.adapter.persistence.outbox.HarborOutboxRepositoryAdapter
import com.sonicdevelopment.driven.adapter.persistence.outbox.ShippingOutboxRepositoryAdapter
import com.sonicdevelopment.driven.adapter.persistence.savings.SavingsRepositoryAdapter
import com.sonicdevelopment.driven.adapter.persistence.ship.ShipRepositoryAdapter
import com.sonicdevelopment.driven.adapter.persistence.stock.StockRepositoryAdapter
import com.sonicdevelopment.domain.exception.ShipNotIncomingException
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
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.TransactionTemplate
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

    /**
     * The real outbox, except that [broadcastShipping] fails while [armed] and [announceShipArrived] fails while
     * [shipArrivedArmed].
     */
    class FailingShippingOutbox(private val real: ShippingOutboxRepositoryAdapter) : ShippingOutboxRepository by real {
        var armed = false
        var shipArrivedArmed = false

        override fun broadcastShipping(ship: Ship, originHarbor: HarborName) {
            check(!armed) { "The outbox write fails" }
            real.broadcastShipping(ship, originHarbor)
        }

        override fun announceShipArrived(
            ship: Ship, shippingId: ShippingId, originHarbor: HarborName, destinationHarbor: HarborName
        ) {
            check(!shipArrivedArmed) { "The Ship Arrived write fails" }
            real.announceShipArrived(ship, shippingId, originHarbor, destinationHarbor)
        }
    }

    /**
     * The real fleet, counting the ship saves that went through, so a test can prove a save really ran. While
     * [concurrentUnloadWins], a concurrent unloading of the same ship clears it, and commits, just before this
     * transaction's own conditional clear runs.
     */
    class CountingShipRepository(
        private val real: ShipRepositoryAdapter,
        transactionManager: PlatformTransactionManager,
    ) : ShipRepositoryPort by real {
        var savesThrough = 0
        var concurrentUnloadWins = false
        private val concurrentTransaction = TransactionTemplate(transactionManager).apply {
            propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW
        }

        override fun saveNewShip(ship: ShipRepositoryPort.InitialShipInformation) {
            real.saveNewShip(ship)
            savesThrough++
        }

        override fun unloadIncomingShip(shipId: ShipId): Boolean {
            if (concurrentUnloadWins) {
                check(concurrentTransaction.execute { real.unloadIncomingShip(shipId) } == true) {
                    "The concurrent unloading did not clear the ship"
                }
            }
            return real.unloadIncomingShip(shipId)
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

    /** The real Harbor outbox, except that [publishHarborOpened] fails while [armed]. */
    class FailingHarborOutbox(private val real: HarborOutboxRepositoryAdapter) : HarborOutboxRepositoryPort by real {
        var armed = false

        override fun publishHarborOpened(harborName: HarborName) {
            check(!armed) { "The Harbor outbox write fails" }
            real.publishHarborOpened(harborName)
        }
    }

    /** The real Savings, counting the payments that went through, so a test can prove a payment really ran. */
    class CountingSavings(private val real: SavingsRepositoryAdapter) : SavingsRepositoryPort by real {
        var paysThrough = 0

        override fun pay(amount: Money): Boolean = real.pay(amount).also { if (it) paysThrough++ }
    }

    @TestConfiguration
    class FailingDrivenPorts {
        @Bean
        @Primary
        fun failingHarborOutbox(real: HarborOutboxRepositoryAdapter) = FailingHarborOutbox(real)

        @Bean
        @Primary
        fun failingShippingOutbox(real: ShippingOutboxRepositoryAdapter) = FailingShippingOutbox(real)

        @Bean
        @Primary
        fun failingStock(real: StockRepositoryAdapter) = FailingStock(real)

        @Bean
        @Primary
        fun countingSavings(real: SavingsRepositoryAdapter) = CountingSavings(real)

        @Bean
        @Primary
        fun countingShipRepository(real: ShipRepositoryAdapter, transactionManager: PlatformTransactionManager) =
            CountingShipRepository(real, transactionManager)
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

    @Autowired
    lateinit var failingHarborOutbox: FailingHarborOutbox

    @Autowired
    lateinit var harborManagementPort: HarborManagementPort

    @Autowired
    lateinit var marketPort: MarketPort

    @Autowired
    lateinit var countingSavings: CountingSavings

    @Autowired
    lateinit var incomingShipManagementPort: IncomingShipManagementPort

    @Autowired
    lateinit var countingShipRepository: CountingShipRepository

    @BeforeEach
    fun everythingWorksAgain() {
        jdbcTemplate.truncateMutableTables()
        jdbcTemplate.resetStockToStartingStock()
        jdbcTemplate.resetSavingsToStartingSavings()
        countingSavings.paysThrough = 0
        countingShipRepository.savesThrough = 0
        countingShipRepository.concurrentUnloadWins = false
        failingOutbox.armed = false
        failingOutbox.shipArrivedArmed = false
        failingStock.armed = false
        failingHarborOutbox.armed = false
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
    fun `An Arrival whose Ship Arrived write fails leaves no inbox row, Arrival, fleet, Cargo aboard or outbox change`() {
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
        failingOutbox.shipArrivedArmed = true

        shouldThrow<IllegalStateException> {
            arrivalManagementPort.receiveShippingPublished(EventId(eventId), arrival)
        }

        // the ship and its Cargo aboard really reached the database before the Ship Arrived write failed
        countingShipRepository.savesThrough shouldBe 1
        jdbcTemplate.queryForObject(
            "SELECT count(*) FROM inbox_events WHERE event_id = ?", Int::class.java, eventId
        ) shouldBe 0
        jdbcTemplate.queryForObject(
            "SELECT count(*) FROM arrivals WHERE shipping_id = ?", Int::class.java, arrival.shippingId.id
        ) shouldBe 0
        count("ships") shouldBe 0
        count("ships_cargos_aboard") shouldBe 0
        availableShips().shouldBeEmpty()
        jdbcTemplate.queryForList("SELECT stock_quantity FROM stocks", Int::class.java)
            .forEach { it shouldBe STARTING_STOCK }
        count("shipping_outbox") shouldBe 0
    }

    @Test
    fun `An opening whose Harbor Opened write fails leaves no Price and no outbox row`() {
        val startupPrices = jdbcTemplate.queryForList("SELECT cargo_id, price_amount FROM prices")
        try {
            jdbcTemplate.update("DELETE FROM prices")
            failingHarborOutbox.armed = true

            shouldThrow<IllegalStateException> { harborManagementPort.openHarbor() }

            count("prices") shouldBe 0
            count("shipping_outbox") shouldBe 0
        } finally {
            // Leave the startup's Prices for the other tests sharing the database
            jdbcTemplate.update("DELETE FROM prices")
            startupPrices.forEach {
                jdbcTemplate.update(
                    "INSERT INTO prices (id, cargo_id, price_amount) VALUES (nextval('prices_seq'), ?, ?)", it["cargo_id"], it["price_amount"]
                )
            }
        }
    }

    @Test
    fun `A purchase whose Stock write fails leaves the Savings and the Stock unchanged`() {
        failingStock.armed = true
        failingStock.succeedingPuts = 0

        // any rolled Price of Ale, times 2, is covered by the Starting Savings
        shouldThrow<IllegalStateException> {
            marketPort.buyCargo(CargoId(SeedData.cargoIdOf("Ale")), 2)
        }

        // the payment really reached the database before the Stock write failed
        countingSavings.paysThrough shouldBe 1
        jdbcTemplate.queryForObject("SELECT savings_amount FROM savings", java.math.BigDecimal::class.java)!!
            .toPlainString() shouldBe STARTING_SAVINGS
        jdbcTemplate.queryForList("SELECT stock_quantity FROM stocks", Int::class.java)
            .forEach { it shouldBe STARTING_STOCK }
    }

    @Test
    fun `An unloading whose second Stock write fails leaves the Savings, the Stock, the Cargo aboard and the Incoming flag unchanged`() {
        val saltyWhisker = anIncomingShipWithTwoRumAndOneSugar()
        failingStock.armed = true
        failingStock.succeedingPuts = 1

        shouldThrow<IllegalStateException> { incomingShipManagementPort.unloadIncomingShip(saltyWhisker) }

        // the payment and the first Stock write really reached the database before the second Stock write failed
        countingSavings.paysThrough shouldBe 1
        failingStock.putsThrough shouldBe 1
        savingsAmount() shouldBe STARTING_SAVINGS
        jdbcTemplate.queryForList("SELECT stock_quantity FROM stocks", Int::class.java)
            .forEach { it shouldBe STARTING_STOCK }
        count("ships_cargos_aboard") shouldBe 3
        shipIncoming(saltyWhisker) shouldBe true
    }

    @Test
    fun `An unloading that a concurrent one beat to the ship rolls back its payment and its Stock writes`() {
        val saltyWhisker = anIncomingShipWithTwoRumAndOneSugar()
        countingShipRepository.concurrentUnloadWins = true

        shouldThrow<ShipNotIncomingException> { incomingShipManagementPort.unloadIncomingShip(saltyWhisker) }

        // this unloading really paid and stocked before its conditional clear found nothing to clear
        countingSavings.paysThrough shouldBe 1
        failingStock.putsThrough shouldBe 3
        savingsAmount() shouldBe STARTING_SAVINGS
        jdbcTemplate.queryForList("SELECT stock_quantity FROM stocks", Int::class.java)
            .forEach { it shouldBe STARTING_STOCK }
        // the concurrent unloading's clear stays
        count("ships_cargos_aboard") shouldBe 0
        shipIncoming(saltyWhisker) shouldBe false
    }

    private fun anIncomingShipWithTwoRumAndOneSugar(): ShipId {
        val rum = CargoId(SeedData.cargoIdOf("Rum"))
        val arrival = ShippingPublishedDTO(
            shipId = ShipId(UUID.randomUUID()),
            shipName = "Salty Whisker",
            catainId = CatainId(SeedData.aCatainId),
            shippingId = ShippingId(UUID.randomUUID()),
            cargoIds = listOf(rum, rum, CargoId(SeedData.cargoIdOf("Sugar"))),
            originHarbor = HarborName("Tortuga"),
            destinationHarbor = HarborName("Port Royal"),
        )
        arrivalManagementPort.receiveShippingPublished(EventId(UUID.randomUUID()), arrival)
        shipIncoming(arrival.shipId) shouldBe true
        count("ships_cargos_aboard") shouldBe 3
        return arrival.shipId
    }

    private fun savingsAmount(): String =
        jdbcTemplate.queryForObject("SELECT savings_amount FROM savings", java.math.BigDecimal::class.java)!!.toPlainString()

    private fun shipIncoming(shipId: ShipId): Boolean =
        jdbcTemplate.queryForObject("SELECT ship_incoming FROM ships WHERE ship_id = ?", Boolean::class.java, shipId.id)!!

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
