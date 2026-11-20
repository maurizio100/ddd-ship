package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.exception.HireRefusedException
import com.sonicdevelopment.domain.exception.SavingsDoNotCoverException
import com.sonicdevelopment.domain.fixtures.aShip
import com.sonicdevelopment.domain.model.Recruit
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.domain.model.values.RecruitId
import com.sonicdevelopment.domain.ports.driven.RecruitPoolRepositoryPort
import com.sonicdevelopment.domain.ports.driven.SavingsRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Test
import java.util.UUID

class CrewManagementServiceTest {

    private val portRoyal = HarborName("Port Royal")
    private val ships = mockk<ShipRepositoryPort>(relaxed = true)
    private val savings = mockk<SavingsRepositoryPort>()
    private val pool = mockk<RecruitPoolRepositoryPort>()
    private val generator = RecruitPoolGenerator(portRoyal)
    private val service = CrewManagementService(portRoyal, ships, savings, pool, generator)

    private val mittens = Recruit(RecruitId(UUID.randomUUID()), "Mittens", "Strong Paws")
    private val ship = aShip()

    private fun nothingWasWritten() {
        verify(exactly = 0) { savings.pay(any()) }
        verify(exactly = 0) { pool.takeRecruit(any()) }
        verify(exactly = 0) { ships.addCrewMember(any(), any()) }
        verify(exactly = 0) { pool.rememberRecruit(any(), any()) }
    }

    private fun givenMittensIsOffered() {
        every { ships.lockShip(ship.id) } returns ship
        every { pool.getRecruits() } returns listOf(mittens)
    }

    @Test
    fun `hire locks the ship, pays the Hiring Fee, takes the Recruit, adds the Crew Member and offers the next ordinal`() {
        givenMittensIsOffered()
        every { savings.pay(HiringFee.AMOUNT) } returns true
        every { savings.getSavings() } returns Money.of("950.00")
        every { pool.takeRecruit(mittens.id) } returns true
        every { pool.nextOrdinal() } returns 5
        every { pool.rememberRecruit(5, any()) } returns true

        val hire = service.hire(ship.id, mittens.id)!!

        hire.crewMember.id shouldBe mittens.id.id
        hire.hiringFee shouldBe HiringFee.AMOUNT
        hire.savings shouldBe Money.of("950.00")
        hire.newRecruit.id shouldBe generator.recruit(5).id.id
        verifyOrder {
            ships.lockShip(ship.id)
            savings.pay(HiringFee.AMOUNT)
            pool.takeRecruit(mittens.id)
            ships.addCrewMember(ship.id, match { it.id.id == mittens.id.id })
            pool.nextOrdinal()
            pool.rememberRecruit(5, generator.recruit(5))
        }
    }

    @Test
    fun `a refused guard pays nothing and writes nothing`() {
        every { ships.lockShip(ship.id) } returns aShip(homeHarbor = HarborName("Tortuga"))
        every { pool.getRecruits() } returns listOf(mittens)

        shouldThrow<HireRefusedException> { service.hire(ship.id, mittens.id) }
        nothingWasWritten()
    }

    @Test
    fun `a Recruit that is not offered is refused and nothing is written`() {
        every { ships.lockShip(ship.id) } returns ship
        every { pool.getRecruits() } returns emptyList()

        shouldThrow<HireRefusedException> { service.hire(ship.id, mittens.id) }
        nothingWasWritten()
    }

    @Test
    fun `Savings that fall short take no Recruit and add no Crew Member`() {
        givenMittensIsOffered()
        every { savings.pay(any()) } returns false

        shouldThrow<SavingsDoNotCoverException> { service.hire(ship.id, mittens.id) }
            .message shouldBe "The Savings do not cover the Hiring Fee of 50.00 $"
        verify(exactly = 0) { pool.takeRecruit(any()) }
        verify(exactly = 0) { ships.addCrewMember(any(), any()) }
    }

    @Test
    fun `a Recruit taken concurrently throws after the payment so the transaction rolls back`() {
        givenMittensIsOffered()
        every { savings.pay(any()) } returns true
        every { pool.takeRecruit(any()) } returns false

        shouldThrow<HireRefusedException> { service.hire(ship.id, mittens.id) }
        verify(exactly = 1) { savings.pay(HiringFee.AMOUNT) }
        verify(exactly = 0) { ships.addCrewMember(any(), any()) }
    }

    @Test
    fun `a ship not in the fleet returns null`() {
        every { ships.lockShip(ship.id) } returns null

        service.hire(ship.id, mittens.id) shouldBe null
        nothingWasWritten()
    }

    @Test
    fun `the top-up retries on a taken ordinal`() {
        givenMittensIsOffered()
        every { savings.pay(any()) } returns true
        every { savings.getSavings() } returns Money.of("950.00")
        every { pool.takeRecruit(any()) } returns true
        every { pool.nextOrdinal() } returnsMany listOf(5, 6)
        every { pool.rememberRecruit(5, any()) } returns false
        every { pool.rememberRecruit(6, any()) } returns true

        service.hire(ship.id, mittens.id)!!.newRecruit.id shouldBe generator.recruit(6).id.id
    }

    @Test
    fun `hire is Transactional and does not exclude any exception from the rollback`() {
        val hire = CrewManagementService::class.java.getMethod("hire", com.sonicdevelopment.domain.model.values.ShipId::class.java, RecruitId::class.java)

        val transactional = hire.getAnnotation(Transactional::class.java)

        (transactional != null) shouldBe true
        transactional.dontRollbackOn.toList() shouldBe emptyList()
    }
}
