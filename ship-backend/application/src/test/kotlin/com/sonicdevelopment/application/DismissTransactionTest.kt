package com.sonicdevelopment.application

import com.sonicdevelopment.application.acceptance.fixtures.aShipBeingPrepared
import com.sonicdevelopment.application.acceptance.fixtures.resetSavingsToStartingSavings
import com.sonicdevelopment.application.acceptance.fixtures.resetStockToStartingStock
import com.sonicdevelopment.application.acceptance.fixtures.truncateMutableTables
import com.sonicdevelopment.domain.exception.DismissRefusedException
import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.values.CrewMemberId
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort
import com.sonicdevelopment.domain.ports.driving.ship.CrewManagementPort
import com.sonicdevelopment.domain.ports.driving.ship.DismissalDTO
import com.sonicdevelopment.driven.adapter.persistence.ship.ShipRepositoryAdapter
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
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
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/**
 * A Dismiss refunds half the Hiring Fee under the ship's row lock, after its guards. Two concurrent Dismissals of one
 * Crew Member must refund once; the in-memory fakes cannot race, so this is pinned against the real database.
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
class DismissTransactionTest {

    /** The real fleet, except that [afterLock] runs once, right after a [lockShip] got the row lock. */
    class HoldingShipRepository(private val real: ShipRepositoryAdapter) : ShipRepositoryPort by real {
        @Volatile
        var afterLock: (() -> Unit)? = null

        override fun lockShip(shipId: ShipId): Ship? {
            val ship = real.lockShip(shipId)
            val hook = afterLock
            if (hook != null) {
                afterLock = null
                hook()
            }
            return ship
        }
    }

    @TestConfiguration
    class HoldingDrivenPorts {
        @Bean
        @Primary
        fun holdingShipRepository(real: ShipRepositoryAdapter) = HoldingShipRepository(real)
    }

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    lateinit var crewManagementPort: CrewManagementPort

    @Autowired
    lateinit var holdingShips: HoldingShipRepository

    @BeforeEach
    fun reset() {
        jdbcTemplate.truncateMutableTables()
        jdbcTemplate.resetStockToStartingStock()
        jdbcTemplate.resetSavingsToStartingSavings()
        holdingShips.afterLock = null
    }

    @Test
    fun `Two concurrent Dismissals of the same Crew Member - refunded once`() {
        val shipId = restTemplate.aShipBeingPrepared("Salty Whisker")
        val crewMemberId = UUID.randomUUID()
        jdbcTemplate.update(
            "INSERT INTO crew_members (id, crew_member_id, crew_member_name, crew_member_trait, ship_id) " +
                "SELECT nextval('crew_members_seq'), ?, 'Mittens', 'Strong Paws', id FROM ships WHERE ship_id = ?",
            crewMemberId, shipId
        )
        val firstHoldsTheLock = CountDownLatch(1)
        val secondStarted = CountDownLatch(1)
        holdingShips.afterLock = {
            firstHoldsTheLock.countDown()
            secondStarted.await(10, TimeUnit.SECONDS)
            Thread.sleep(LOCK_WAIT_MILLIS)
        }
        val executor = Executors.newFixedThreadPool(2)
        try {
            val first = executor.submit<Any?> { crewManagementPort.dismiss(ShipId(shipId), CrewMemberId(crewMemberId)) }
            val second = executor.submit<Any?> {
                firstHoldsTheLock.await(10, TimeUnit.SECONDS)
                secondStarted.countDown()
                crewManagementPort.dismiss(ShipId(shipId), CrewMemberId(crewMemberId))
            }

            first.get(30, TimeUnit.SECONDS).shouldBeInstanceOf<DismissalDTO>().refund.toDecimalString() shouldBe "25.00"
            shouldThrow<DismissRefusedException> { second.unwrap() }

            jdbcTemplate.queryForObject("SELECT count(*) FROM crew_members", Int::class.java) shouldBe 0
            jdbcTemplate.queryForObject("SELECT savings_amount::text FROM savings", String::class.java) shouldBe "1025.00"
        } finally {
            executor.shutdown()
        }
    }

    private fun Future<Any?>.unwrap(): Any? =
        try {
            get(30, TimeUnit.SECONDS)
        } catch (e: ExecutionException) {
            throw e.cause ?: e
        }

    private companion object {
        const val LOCK_WAIT_MILLIS = 1000L
    }
}
