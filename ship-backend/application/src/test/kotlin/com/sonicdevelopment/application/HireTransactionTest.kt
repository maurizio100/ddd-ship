package com.sonicdevelopment.application

import com.sonicdevelopment.application.acceptance.fixtures.aShipBeingPrepared
import com.sonicdevelopment.application.acceptance.fixtures.resetSavingsToStartingSavings
import com.sonicdevelopment.application.acceptance.fixtures.resetStockToStartingStock
import com.sonicdevelopment.application.acceptance.fixtures.truncateMutableTables
import com.sonicdevelopment.domain.exception.HireRefusedException
import com.sonicdevelopment.domain.model.CrewMember
import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.domain.model.values.RecruitId
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.ports.driven.RecruitPoolRepositoryPort
import com.sonicdevelopment.domain.ports.driven.SavingsRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort
import com.sonicdevelopment.domain.ports.driving.ship.CrewManagementPort
import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.service.RecruitPoolGenerator
import com.sonicdevelopment.driven.adapter.persistence.recruit.RecruitPoolRepositoryAdapter
import com.sonicdevelopment.driven.adapter.persistence.savings.SavingsRepositoryAdapter
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
import java.util.concurrent.TimeUnit
import java.util.concurrent.Future

/**
 * Hiring pays the Hiring Fee, takes the Recruit and adds the Crew Member in one transaction, under the ship's row
 * lock (ADR-0008, ADR-0009). The in-memory fakes of the acceptance tests cannot roll back or race, so these
 * guarantees are pinned against the real database. The concurrent Hires run on their own threads: on one thread the
 * second would wait forever for the row lock the first holds.
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
class HireTransactionTest {

    /**
     * The real fleet, except that [addCrewMember] fails while [addCrewMemberFails], and that [afterLock] runs once,
     * right after a [lockShip] got the row lock.
     */
    class HoldingShipRepository(private val real: ShipRepositoryAdapter) : ShipRepositoryPort by real {
        var addCrewMemberFails = false

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

        override fun addCrewMember(shipId: ShipId, crewMember: CrewMember) {
            check(!addCrewMemberFails) { "The Crew write fails" }
            real.addCrewMember(shipId, crewMember)
        }
    }

    /** The real Savings, counting the payments that went through. */
    class PayCountingSavings(private val real: SavingsRepositoryAdapter) : SavingsRepositoryPort by real {
        var paysThrough = 0

        override fun pay(amount: Money): Boolean = real.pay(amount).also { if (it) paysThrough++ }
    }

    /** The real Recruit Pool, except that [afterTake] runs once, right after a [takeRecruit] marked a Recruit hired. */
    class HoldingRecruitPool(private val real: RecruitPoolRepositoryAdapter) : RecruitPoolRepositoryPort by real {
        @Volatile
        var afterTake: (() -> Unit)? = null

        override fun takeRecruit(recruitId: RecruitId): Boolean {
            val taken = real.takeRecruit(recruitId)
            val hook = afterTake
            if (taken && hook != null) {
                afterTake = null
                hook()
            }
            return taken
        }
    }

    @TestConfiguration
    class HoldingDrivenPorts {
        @Bean
        @Primary
        fun holdingShipRepository(real: ShipRepositoryAdapter) = HoldingShipRepository(real)

        @Bean
        @Primary
        fun payCountingSavings(real: SavingsRepositoryAdapter) = PayCountingSavings(real)

        @Bean
        @Primary
        fun holdingRecruitPool(real: RecruitPoolRepositoryAdapter) = HoldingRecruitPool(real)
    }

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    lateinit var crewManagementPort: CrewManagementPort

    @Autowired
    lateinit var holdingShips: HoldingShipRepository

    @Autowired
    lateinit var countingSavings: PayCountingSavings

    @Autowired
    lateinit var holdingPool: HoldingRecruitPool

    @Autowired
    lateinit var recruitPoolGenerator: RecruitPoolGenerator

    private lateinit var pool: List<RecruitId>

    @BeforeEach
    fun fiveRecruitsAreOffered() {
        jdbcTemplate.truncateMutableTables()
        jdbcTemplate.resetStockToStartingStock()
        jdbcTemplate.resetSavingsToStartingSavings()
        jdbcTemplate.update("DELETE FROM recruits")
        pool = (0..4).map { ordinal ->
            val recruit = recruitPoolGenerator.recruit(ordinal)
            jdbcTemplate.update(
                "INSERT INTO recruits (id, recruit_id, ordinal, name, trait) " +
                    "VALUES (nextval('recruits_seq'), ?, ?, ?, ?)",
                recruit.id.id, ordinal, recruit.name, recruit.trait
            )
            recruit.id
        }
        holdingShips.addCrewMemberFails = false
        holdingShips.afterLock = null
        holdingPool.afterTake = null
        countingSavings.paysThrough = 0
    }

    @Test
    fun `A Hire whose Crew write fails leaves the Savings, the Recruit Pool and the Crew unchanged`() {
        val shipId = restTemplate.aShipBeingPrepared("Salty Whisker")
        holdingShips.addCrewMemberFails = true

        shouldThrow<IllegalStateException> { crewManagementPort.hire(ShipId(shipId), pool[0]) }

        countingSavings.paysThrough shouldBe 1
        savings() shouldBe "1000.00"
        offeredRecruits() shouldBe pool.map { it.id }
        crewRows() shouldBe 0
        hiredRecruits() shouldBe 0
    }

    @Test
    fun `Two concurrent Hires onto a ship with one free berth - only one takes effect`() {
        val shipId = restTemplate.aShipBeingPrepared("Salty Whisker")
        // a Sloop carries 2 Crew Members: one berth is already taken
        jdbcTemplate.update(
            "INSERT INTO crew_members (id, crew_member_id, crew_member_name, crew_member_trait, ship_id) " +
                "SELECT nextval('crew_members_seq'), ?, 'Old Hand', 'Steady', id FROM ships WHERE ship_id = ?",
            UUID.randomUUID(), shipId
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
            val first = executor.submit<Any?> { crewManagementPort.hire(ShipId(shipId), pool[0]) }
            val second = executor.submit<Any?> {
                firstHoldsTheLock.await(10, TimeUnit.SECONDS)
                secondStarted.countDown()
                crewManagementPort.hire(ShipId(shipId), pool[1])
            }

            first.get(30, TimeUnit.SECONDS).shouldBeInstanceOf<Any>()
            shouldThrow<HireRefusedException> { second.unwrap() }

            crewRows() shouldBe 2
            savings() shouldBe "950.00"
            offeredRecruits().size shouldBe 5
            hiredRecruits() shouldBe 1
        } finally {
            executor.shutdown()
        }
    }

    @Test
    fun `Two concurrent Hires of the same Recruit onto two ships - hired once, paid once`() {
        val firstShip = restTemplate.aShipBeingPrepared("Salty Whisker")
        val secondShip = restTemplate.aShipBeingPrepared("Old Barnacle")
        val firstHasTakenTheRecruit = CountDownLatch(1)
        val secondStarted = CountDownLatch(1)
        holdingPool.afterTake = {
            firstHasTakenTheRecruit.countDown()
            secondStarted.await(10, TimeUnit.SECONDS)
            Thread.sleep(LOCK_WAIT_MILLIS)
        }
        val executor = Executors.newFixedThreadPool(2)
        try {
            val first = executor.submit<Any?> { crewManagementPort.hire(ShipId(firstShip), pool[0]) }
            val second = executor.submit<Any?> {
                firstHasTakenTheRecruit.await(10, TimeUnit.SECONDS)
                secondStarted.countDown()
                crewManagementPort.hire(ShipId(secondShip), pool[0])
            }

            first.get(30, TimeUnit.SECONDS).shouldBeInstanceOf<Any>()
            shouldThrow<HireRefusedException> { second.unwrap() }

            crewRows() shouldBe 1
            savings() shouldBe "950.00"
            hiredRecruits() shouldBe 1
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

    private fun savings(): String = jdbcTemplate.queryForObject("SELECT savings_amount::text FROM savings", String::class.java)!!

    private fun crewRows(): Int = jdbcTemplate.queryForObject("SELECT count(*) FROM crew_members", Int::class.java)!!

    private fun hiredRecruits(): Int =
        jdbcTemplate.queryForObject("SELECT count(*) FROM recruits WHERE recruit_hired", Int::class.java)!!

    private fun offeredRecruits(): List<UUID> =
        jdbcTemplate.queryForList("SELECT recruit_id FROM recruits WHERE NOT recruit_hired ORDER BY ordinal", UUID::class.java)

    private companion object {
        const val LOCK_WAIT_MILLIS = 1000L
    }
}
