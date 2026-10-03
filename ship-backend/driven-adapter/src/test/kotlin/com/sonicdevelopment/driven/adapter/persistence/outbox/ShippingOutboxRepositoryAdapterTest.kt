package com.sonicdevelopment.driven.adapter.persistence.outbox

import com.fasterxml.jackson.databind.ObjectMapper
import com.sonicdevelopment.domain.model.Cargo
import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.values.CargoId
import com.sonicdevelopment.domain.model.values.CatainId
import com.sonicdevelopment.domain.model.values.ShippingQuote
import com.sonicdevelopment.driven.adapter.PostgresTestcontainer
import com.sonicdevelopment.driven.adapter.persistence.outbox.events.ShippingEvent
import com.sonicdevelopment.driven.adapter.persistence.outbox.events.ShippingEventConverter
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import java.util.*

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgresTestcontainer::class, ShippingOutboxRepositoryAdapter::class)
class ShippingOutboxRepositoryAdapterTest {

    @Autowired
    lateinit var shippingOutbox: ShippingOutboxRepositoryAdapter

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    lateinit var entityManager: TestEntityManager

    @BeforeEach
    fun truncateOutbox() {
        jdbcTemplate.execute("TRUNCATE TABLE shipping_outbox")
    }

    @Test
    fun `writes shipping-published with an unchanged payload`() {
        val ship = aReleasedShip()

        shippingOutbox.broadcastShipping(ship)
        entityManager.flush()

        val row = jdbcTemplate.queryForMap("SELECT aggregate_type, aggregate_id, event_type, payload FROM shipping_outbox")
        row["aggregate_type"] shouldBe "shipping"
        row["aggregate_id"] shouldBe ship.activeShipping!!.id.id
        row["event_type"] shouldBe "shipping-published"
        val payload = row["payload"] as String
        ObjectMapper().readTree(payload).fieldNames().asSequence().toList() shouldContainExactlyInAnyOrder
            listOf("shipEventData", "shippingEventData", "catain")
        ObjectMapper().readValue(payload, ShippingEvent::class.java) shouldBe ShippingEventConverter.toShippingEvent(ship)
    }

    private fun aReleasedShip(): Ship {
        val ship = Ship(name = "Black Pearl", catainId = CatainId(UUID.randomUUID()), catainName = "Jack Sparrow")
        ship.addCargo(Cargo(CargoId(UUID.randomUUID()), "Rum", 5.0f))
        ship.createNewShipping()
        ship.release(ShippingQuote("Fair winds"))
        return ship
    }
}
