package com.sonicdevelopment.driven.adapter.persistence.savings

import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.driven.adapter.DbTest
import com.sonicdevelopment.driven.adapter.PostgresTestcontainer
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgresTestcontainer::class, SavingsRepositoryAdapter::class)
/** Each test runs in a transaction that is rolled back, so a changed Savings row does not leak. */
@DbTest
class SavingsRepositoryAdapterTest {

    @Autowired
    lateinit var savings: SavingsRepositoryAdapter

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

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
}
