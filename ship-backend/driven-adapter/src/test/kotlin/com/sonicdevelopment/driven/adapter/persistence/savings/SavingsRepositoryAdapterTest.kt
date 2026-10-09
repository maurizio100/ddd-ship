package com.sonicdevelopment.driven.adapter.persistence.savings

import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.driven.adapter.DbTest
import com.sonicdevelopment.driven.adapter.PostgresTestcontainer
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.IllegalTransactionStateException
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgresTestcontainer::class, SavingsRepositoryAdapter::class)
/**
 * Each test runs in a transaction that is rolled back, so a changed Savings row does not leak. The
 * concurrency test commits, and puts the Starting Savings back itself.
 */
@DbTest
class SavingsRepositoryAdapterTest {

    @Autowired
    lateinit var savings: SavingsRepositoryAdapter

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    lateinit var transactionManager: PlatformTransactionManager

    @Test
    fun `the migrated database holds the Starting Savings`() {
        jdbcTemplate.queryForObject("SELECT count(*) FROM savings", Int::class.java) shouldBe 1

        savings.getSavings() shouldBe Money.of("1000.00")
        savings.getSavings().toDecimalString() shouldBe "1000.00"
    }

    @Test
    fun `reads the Savings back exactly`() {
        jdbcTemplate.update("UPDATE savings SET savings_amount = 640.50")

        savings.getSavings().toDecimalString() shouldBe "640.50"
    }

    @Test
    fun `paying takes exactly the amount out of the Savings`() {
        setSavings("150.00")

        val paid = savings.pay(Money.of("100.00"))

        paid shouldBe true
        storedSavings() shouldBe "50.00"
    }

    @Test
    fun `paying exactly what the Savings hold empties them`() {
        setSavings("100.00")

        savings.pay(Money.of("100.00")) shouldBe true

        storedSavings() shouldBe "0.00"
    }

    @Test
    fun `paying more than the Savings hold returns false and changes nothing`() {
        setSavings("80.00")

        val paid = savings.pay(Money.of("100.00"))

        paid shouldBe false
        storedSavings() shouldBe "80.00"
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun `refuses to pay outside a transaction`() {
        shouldThrow<IllegalTransactionStateException> { savings.pay(Money.of("1.00")) }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun `two concurrent payments the Savings can cover only once let exactly one succeed`() {
        setSavings(STARTING_SAVINGS)
        val transaction = TransactionTemplate(transactionManager)
        val firstHoldsTheRow = CountDownLatch(1)
        val secondStarted = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(2)
        try {
            val first = executor.submit<Boolean> {
                transaction.execute {
                    val paid = savings.pay(Money.of("600.00"))
                    firstHoldsTheRow.countDown()
                    // commit only once the second payment is under way and waits for this row lock
                    secondStarted.await(10, TimeUnit.SECONDS)
                    Thread.sleep(LOCK_WAIT_MILLIS)
                    paid
                }
            }
            val second = executor.submit<Boolean> {
                firstHoldsTheRow.await(10, TimeUnit.SECONDS)
                transaction.execute {
                    secondStarted.countDown()
                    savings.pay(Money.of("600.00"))
                }
            }

            val results = listOf(first.get(30, TimeUnit.SECONDS), second.get(30, TimeUnit.SECONDS))

            results shouldBe listOf(true, false)
            storedSavings() shouldBe "400.00"
        } finally {
            executor.shutdown()
            setSavings(STARTING_SAVINGS)
        }
    }

    private fun setSavings(decimal: String) {
        jdbcTemplate.update("UPDATE savings SET savings_amount = ?::numeric", decimal)
    }

    private fun storedSavings(): String =
        jdbcTemplate.queryForObject("SELECT savings_amount FROM savings", java.math.BigDecimal::class.java)!!.toPlainString()

    private companion object {
        const val STARTING_SAVINGS = "1000.00"
        const val LOCK_WAIT_MILLIS = 500L
    }
}
