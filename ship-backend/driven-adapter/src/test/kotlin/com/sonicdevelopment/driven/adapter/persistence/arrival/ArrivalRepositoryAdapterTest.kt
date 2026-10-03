package com.sonicdevelopment.driven.adapter.persistence.arrival

import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.model.values.ShippingId
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
import java.util.*

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgresTestcontainer::class, ArrivalRepositoryAdapter::class)
class ArrivalRepositoryAdapterTest {

    @Autowired
    lateinit var arrivals: ArrivalRepositoryAdapter

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    private val blackPearl = ShipId(UUID.randomUUID())

    @BeforeEach
    fun truncateArrivals() {
        jdbcTemplate.execute("TRUNCATE TABLE arrivals")
    }

    @Test
    fun `records the Arrival of a Shipping that has not arrived yet`() {
        val shippingId = ShippingId(UUID.randomUUID())

        val recorded = arrivals.recordArrival(shippingId, blackPearl)

        recorded shouldBe true
        jdbcTemplate.queryForObject(
            "SELECT ship_id FROM arrivals WHERE shipping_id = ?", UUID::class.java, shippingId.id
        ) shouldBe blackPearl.id
    }

    @Test
    fun `reports a Shipping that has already arrived`() {
        val shippingId = ShippingId(UUID.randomUUID())
        arrivals.recordArrival(shippingId, blackPearl)

        val recordedAgain = arrivals.recordArrival(shippingId, blackPearl)

        recordedAgain shouldBe false
        jdbcTemplate.queryForObject(
            "SELECT count(*) FROM arrivals WHERE shipping_id = ?", Int::class.java, shippingId.id
        ) shouldBe 1
    }

    @Test
    fun `the same ship arrives again on another Shipping`() {
        arrivals.recordArrival(ShippingId(UUID.randomUUID()), blackPearl)

        arrivals.recordArrival(ShippingId(UUID.randomUUID()), blackPearl) shouldBe true
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun `refuses to record outside a transaction`() {
        shouldThrow<IllegalTransactionStateException> {
            arrivals.recordArrival(ShippingId(UUID.randomUUID()), blackPearl)
        }
    }
}
