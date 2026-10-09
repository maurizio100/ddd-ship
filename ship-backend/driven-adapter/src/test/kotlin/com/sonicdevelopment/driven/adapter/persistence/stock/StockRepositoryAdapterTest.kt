package com.sonicdevelopment.driven.adapter.persistence.stock

import com.sonicdevelopment.driven.adapter.DbTest
import com.sonicdevelopment.domain.model.values.CargoId
import com.sonicdevelopment.driven.adapter.PostgresTestcontainer
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
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
import java.util.*
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgresTestcontainer::class, StockRepositoryAdapter::class)
@DbTest
class StockRepositoryAdapterTest {

    @Autowired
    lateinit var stock: StockRepositoryAdapter

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    lateinit var transactionManager: PlatformTransactionManager

    @BeforeEach
    fun resetStockToStartingStock() {
        jdbcTemplate.execute("DELETE FROM stocks")
        jdbcTemplate.execute(
            "INSERT INTO stocks (id, cargo_id, stock_quantity) SELECT nextval('stocks_seq'), id, $STARTING_STOCK FROM cargos"
        )
    }

    @Test
    fun `taking from Stock lowers it by one`() {
        val taken = stock.takeOneFromStock(cargoId("Rum"))

        taken shouldBe true
        quantityOf("Rum") shouldBe STARTING_STOCK - 1
    }

    @Test
    fun `taking from an empty Stock returns false and leaves it at 0`() {
        setQuantity("Rum", 0)

        val taken = stock.takeOneFromStock(cargoId("Rum"))

        taken shouldBe false
        quantityOf("Rum") shouldBe 0
    }

    @Test
    fun `putting into Stock adds the quantity`() {
        stock.putIntoStock(cargoId("Rum"), 2)

        quantityOf("Rum") shouldBe STARTING_STOCK + 2
    }

    @Test
    fun `putting a Cargo without a stock row creates it`() {
        jdbcTemplate.update(
            "DELETE FROM stocks WHERE cargo_id = (SELECT id FROM cargos WHERE cargo_name = ?)", "Silk"
        )

        stock.putIntoStock(cargoId("Silk"))

        quantityOf("Silk") shouldBe 1
    }

    @Test
    fun `getStock returns every Cargo's quantity`() {
        setQuantity("Rum", 1)

        val all = stock.getStock()

        all.size shouldBe catalogSize()
        all shouldContain (cargoId("Rum") to 1)
        all shouldContain (cargoId("Ale") to STARTING_STOCK)
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun `refuses to change the Stock outside a transaction`() {
        shouldThrow<IllegalTransactionStateException> { stock.takeOneFromStock(cargoId("Rum")) }
        shouldThrow<IllegalTransactionStateException> { stock.putIntoStock(cargoId("Rum")) }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun `two concurrent takes of the last Cargo let exactly one succeed`() {
        setQuantity("Rum", 1)
        val rum = cargoId("Rum")
        val bothReady = CyclicBarrier(2)
        val transaction = TransactionTemplate(transactionManager)
        val executor = Executors.newFixedThreadPool(2)

        val takes = (1..2).map {
            executor.submit<Boolean> {
                transaction.execute {
                    bothReady.await(10, TimeUnit.SECONDS)
                    stock.takeOneFromStock(rum)
                }
            }
        }
        val results = takes.map { it.get(30, TimeUnit.SECONDS) }
        executor.shutdown()

        results.count { it } shouldBe 1
        quantityOf("Rum") shouldBe 0
    }

    private fun cargoId(cargoName: String): CargoId =
        CargoId(
            jdbcTemplate.queryForObject(
                "SELECT cargo_id FROM cargos WHERE cargo_name = ?", UUID::class.java, cargoName
            )!!
        )

    private fun quantityOf(cargoName: String): Int =
        jdbcTemplate.queryForObject(
            "SELECT s.stock_quantity FROM stocks s JOIN cargos c ON s.cargo_id = c.id WHERE c.cargo_name = ?",
            Int::class.java, cargoName
        )!!

    private fun setQuantity(cargoName: String, quantity: Int) {
        jdbcTemplate.update(
            "UPDATE stocks SET stock_quantity = ? WHERE cargo_id = (SELECT id FROM cargos WHERE cargo_name = ?)",
            quantity, cargoName
        )
    }

    private fun catalogSize(): Int =
        jdbcTemplate.queryForObject("SELECT count(*) FROM cargos", Int::class.java)!!

    private companion object {
        const val STARTING_STOCK = 3
    }
}
