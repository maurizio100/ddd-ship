package com.sonicdevelopment.driven.adapter.persistence.ship

import com.sonicdevelopment.driven.adapter.DbTest
import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.enums.ShippingState
import com.sonicdevelopment.domain.model.values.CatainId
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.model.values.ShippingQuote
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort.InitialShipInformation
import com.sonicdevelopment.driven.adapter.PostgresTestcontainer
import com.sonicdevelopment.driven.adapter.persistence.shipping.ShippingRepositoryAdapter
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.IllegalTransactionStateException
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgresTestcontainer::class, ShipRepositoryAdapter::class, ShippingRepositoryAdapter::class)
@DbTest
class ShipRepositoryAdapterTest {

    @Autowired
    lateinit var ships: ShipRepositoryAdapter

    @Autowired
    lateinit var shippings: ShippingRepositoryAdapter

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    lateinit var entityManager: TestEntityManager

    @BeforeEach
    fun truncateMutableTables() {
        jdbcTemplate.execute("TRUNCATE TABLE ships_cargos, shippings, ships")
    }

    @Test
    fun `a ship removed from the fleet is neither listed nor found, but its Shippings stay readable`() {
        val blackPearl = aShipAtSea("Black Pearl")
        val voyage = blackPearl.activeShipping!!
        blackPearl.endShipping(voyage.id)
        shippings.updateActiveShipping(blackPearl)
        val flyingDutchman = aShip("Flying Dutchman")

        ships.removeFromFleet(blackPearl.id)
        flushAndClear()

        ships.getAllShips().map { it.id } shouldBe listOf(flyingDutchman.id)
        ships.getShipDetails(blackPearl.id) shouldBe null
        val shipping = shippings.getShippingInformation(blackPearl.id, voyage.id)
        shipping shouldNotBe null
        shipping!!.shippingState shouldBe ShippingState.DONE
        rowsFor(blackPearl.id) shouldBe 1
    }

    @Test
    fun `saving a Ship Id that left the fleet takes the same ship back in`() {
        val blackPearl = aShip("Black Pearl")
        ships.removeFromFleet(blackPearl.id)
        flushAndClear()

        ships.saveNewShip(InitialShipInformation.fromShip(renamed(blackPearl, "Black Pearl II")))
        flushAndClear()

        rowsFor(blackPearl.id) shouldBe 1
        ships.getShipDetails(blackPearl.id)!!.shipName shouldBe "Black Pearl II"
        ships.getAllShips().map { it.id } shouldBe listOf(blackPearl.id)
    }

    @Test
    fun `renaming a ship keeps one row`() {
        val blackPearl = aShip("Black Pearl")

        ships.saveNewShip(InitialShipInformation.fromShip(renamed(blackPearl, "Wicked Wench")))
        flushAndClear()

        rowsFor(blackPearl.id) shouldBe 1
        ships.getShipDetails(blackPearl.id)!!.shipName shouldBe "Wicked Wench"
    }

    @Test
    fun `saving a ship stores where it arrived from, and a later Arrival overwrites it`() {
        val blackPearl = anArrivedShip("Black Pearl", from = "Tortuga")
        flushAndClear()

        ships.getAllShips().single().arrivedFrom shouldBe HarborName("Tortuga")
        ships.getShipDetails(blackPearl.id)!!.arrivedFrom shouldBe HarborName("Tortuga")

        ships.removeFromFleet(blackPearl.id)
        flushAndClear()
        ships.saveNewShip(
            InitialShipInformation.fromShip(
                Ship(
                    id = blackPearl.id,
                    name = "Black Pearl",
                    catainId = blackPearl.catainId,
                    catainName = blackPearl.catainName,
                    arrivedFrom = HarborName("Nassau"),
                )
            )
        )
        flushAndClear()

        rowsFor(blackPearl.id) shouldBe 1
        ships.getAllShips().single().arrivedFrom shouldBe HarborName("Nassau")
        ships.getShipDetails(blackPearl.id)!!.arrivedFrom shouldBe HarborName("Nassau")

        val interceptor = aShip("Interceptor")
        flushAndClear()
        ships.getShipDetails(interceptor.id)!!.arrivedFrom shouldBe null
    }

    @Test
    fun `renaming a ship keeps where it arrived from`() {
        val blackPearl = anArrivedShip("Black Pearl", from = "Tortuga")
        flushAndClear()

        val loaded = ships.getShipDetails(blackPearl.id)!!
        loaded.shipName = "Wicked Wench"
        ships.saveNewShip(InitialShipInformation.fromShip(loaded))
        flushAndClear()

        ships.getShipDetails(blackPearl.id)!!.shipName shouldBe "Wicked Wench"
        ships.getShipDetails(blackPearl.id)!!.arrivedFrom shouldBe HarborName("Tortuga")
    }

    @Test
    fun `deleting a ship that left the fleet leaves its row and its DONE Shipping in place`() {
        val blackPearl = aShipAtSea("Black Pearl")
        val voyage = blackPearl.activeShipping!!
        blackPearl.endShipping(voyage.id)
        shippings.updateActiveShipping(blackPearl)
        ships.removeFromFleet(blackPearl.id)
        flushAndClear()

        val deleted = ships.delete(blackPearl.id)
        flushAndClear()

        deleted shouldBe false
        rowsFor(blackPearl.id) shouldBe 1
        shippings.getShippingInformation(blackPearl.id, voyage.id)!!.shippingState shouldBe ShippingState.DONE
    }

    @Test
    fun `deleting a ship in the fleet removes it with its Shippings`() {
        val blackPearl = aShipAtSea("Black Pearl")
        val voyage = blackPearl.activeShipping!!

        val deleted = ships.delete(blackPearl.id)
        flushAndClear()

        deleted shouldBe true
        rowsFor(blackPearl.id) shouldBe 0
        jdbcTemplate.queryForObject(
            "SELECT count(*) FROM shippings WHERE shipping_id = ?", Int::class.java, voyage.id.id
        ) shouldBe 0
    }

    @Test
    fun `deleting an unknown ship reports that there was none`() {
        ships.delete(ShipId(UUID.randomUUID())) shouldBe false
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun `removeFromFleet requires a transaction`() {
        shouldThrow<IllegalTransactionStateException> {
            ships.removeFromFleet(ShipId(UUID.randomUUID()))
        }
        ships.getAllShips().shouldBeEmpty()
    }

    private fun aShip(name: String): Ship {
        val ship = Ship(name = name, catainId = CatainId(seededCatainId()), catainName = "Furry Jones")
        ships.saveNewShip(InitialShipInformation.fromShip(ship))
        entityManager.flush()
        return ship
    }

    private fun anArrivedShip(name: String, from: String): Ship {
        val ship = Ship(
            name = name,
            catainId = CatainId(seededCatainId()),
            catainName = "Furry Jones",
            arrivedFrom = HarborName(from),
        )
        ships.saveNewShip(InitialShipInformation.fromShip(ship))
        entityManager.flush()
        return ship
    }

    private fun aShipAtSea(name: String): Ship {
        val ship = aShip(name)
        ship.createNewShipping()
        shippings.createShipping(ship)
        ship.release(ShippingQuote("Fair winds"), HarborName("Port Royal"))
        shippings.updateActiveShipping(ship)
        entityManager.flush()
        return ship
    }

    private fun renamed(ship: Ship, name: String) =
        Ship(id = ship.id, name = name, catainId = ship.catainId, catainName = ship.catainName)

    private fun seededCatainId(): UUID =
        jdbcTemplate.queryForObject("SELECT catain_id FROM catains ORDER BY id LIMIT 1", UUID::class.java)!!

    private fun flushAndClear() {
        entityManager.flush()
        entityManager.clear()
    }

    private fun rowsFor(shipId: ShipId): Int =
        jdbcTemplate.queryForObject("SELECT count(*) FROM ships WHERE ship_id = ?", Int::class.java, shipId.id)!!
}
