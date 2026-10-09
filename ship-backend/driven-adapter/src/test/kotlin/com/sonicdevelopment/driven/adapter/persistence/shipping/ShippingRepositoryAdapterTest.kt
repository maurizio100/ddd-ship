package com.sonicdevelopment.driven.adapter.persistence.shipping

import com.sonicdevelopment.driven.adapter.DbTest
import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.enums.ShippingState
import com.sonicdevelopment.domain.model.values.CatainId
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.ShippingQuote
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort.InitialShipInformation
import com.sonicdevelopment.driven.adapter.PostgresTestcontainer
import com.sonicdevelopment.driven.adapter.persistence.ship.ShipRepositoryAdapter
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
@Import(PostgresTestcontainer::class, ShippingRepositoryAdapter::class, ShipRepositoryAdapter::class)
@DbTest
class ShippingRepositoryAdapterTest {

    @Autowired
    lateinit var shippings: ShippingRepositoryAdapter

    @Autowired
    lateinit var ships: ShipRepositoryAdapter

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    lateinit var entityManager: TestEntityManager

    @BeforeEach
    fun truncateMutableTables() {
        jdbcTemplate.execute("TRUNCATE TABLE ships_cargos, shippings, ships")
    }

    @Test
    fun `updateActiveShipping stores the Destination Harbor and getShippingInformation reads it back`() {
        val ship = aShipBeingPrepared()

        ship.release(ShippingQuote("Fair winds"), HarborName("Port Royal"))
        shippings.updateActiveShipping(ship)
        entityManager.flush()
        entityManager.clear()

        jdbcTemplate.queryForObject("SELECT destination_harbor FROM shippings", String::class.java) shouldBe "Port Royal"
        val shipping = shippings.getShippingInformation(ship.id, ship.activeShipping!!.id)!!
        shipping.shippingState shouldBe ShippingState.SHIPPING
        shipping.destinationHarbor shouldBe HarborName("Port Royal")
        ships.getShipDetails(ship.id)!!.activeShipping!!.destinationHarbor shouldBe HarborName("Port Royal")
    }

    private fun aShipBeingPrepared(): Ship {
        val catainId = jdbcTemplate.queryForObject("SELECT catain_id FROM catains ORDER BY id LIMIT 1", UUID::class.java)!!
        val ship = Ship(name = "Black Pearl", catainId = CatainId(catainId), catainName = "Furry Jones")
        ships.saveNewShip(InitialShipInformation.fromShip(ship))
        ship.createNewShipping()
        shippings.createShipping(ship)
        entityManager.flush()
        return ship
    }
}
