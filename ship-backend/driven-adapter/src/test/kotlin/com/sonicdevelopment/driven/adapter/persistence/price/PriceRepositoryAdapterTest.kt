package com.sonicdevelopment.driven.adapter.persistence.price

import com.sonicdevelopment.domain.model.values.CargoId
import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.driven.adapter.DbTest
import com.sonicdevelopment.driven.adapter.PostgresTestcontainer
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.IllegalTransactionStateException
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.*

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgresTestcontainer::class, PriceRepositoryAdapter::class)
@DbTest
class PriceRepositoryAdapterTest {

    @Autowired
    lateinit var prices: PriceRepositoryAdapter

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun noPrices() {
        jdbcTemplate.execute("DELETE FROM prices")
    }

    @Test
    fun `remembers a Price exactly`() {
        val remembered = prices.rememberPrice(cargoId("Rum"), Money.of("42.00"))

        remembered shouldBe true
        priceOf("Rum") shouldBe BigDecimal("42.00")
        prices.getPrices() shouldBe mapOf(cargoId("Rum") to Money.of("42.00"))
        prices.getPrices().getValue(cargoId("Rum")).toDecimalString() shouldBe "42.00"
    }

    @Test
    fun `keeps the first Price and reports false for a second one`() {
        prices.rememberPrice(cargoId("Rum"), Money.of("42.00"))

        val remembered = prices.rememberPrice(cargoId("Rum"), Money.of("57.00"))

        remembered shouldBe false
        priceOf("Rum") shouldBe BigDecimal("42.00")
    }

    @Test
    fun `stores nothing for an unknown Cargo`() {
        val remembered = prices.rememberPrice(CargoId(UUID.randomUUID()), Money.of("42.00"))

        remembered shouldBe false
        prices.getPrices() shouldBe emptyMap()
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun `refuses to remember a Price outside a transaction`() {
        shouldThrow<IllegalTransactionStateException> { prices.rememberPrice(cargoId("Rum"), Money.of("42.00")) }
    }

    private fun cargoId(cargoName: String): CargoId =
        CargoId(
            jdbcTemplate.queryForObject(
                "SELECT cargo_id FROM cargos WHERE cargo_name = ?", UUID::class.java, cargoName
            )!!
        )

    private fun priceOf(cargoName: String): BigDecimal =
        jdbcTemplate.queryForObject(
            "SELECT p.price_amount FROM prices p JOIN cargos c ON p.cargo_id = c.id WHERE c.cargo_name = ?",
            BigDecimal::class.java, cargoName
        )!!
}
