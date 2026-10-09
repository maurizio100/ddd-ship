package com.sonicdevelopment.driven.adapter.persistence.ship

import com.sonicdevelopment.driven.adapter.DbTest
import com.sonicdevelopment.domain.model.Cargo
import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.enums.ShippingState
import com.sonicdevelopment.domain.model.values.CargoId
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
        jdbcTemplate.execute("TRUNCATE TABLE ships_cargos_aboard, ships_cargos, shippings, ships")
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

    @Test
    fun `saveNewShip stores the Cargo aboard, the same Cargo twice, and the Incoming flag`() {
        val saltyWhisker = anIncomingShip("Salty Whisker", listOf(rum(), rum(), sugar()))
        flushAndClear()

        val loaded = ships.getShipDetails(saltyWhisker.id)!!
        loaded.isIncoming shouldBe true
        loaded.cargoAboard.map { it.name } shouldBe listOf("Rum", "Rum", "Sugar")
        loaded.cargoAboard.map { it.id } shouldBe listOf(rum().id, rum().id, sugar().id)
        loaded.activeShipping shouldBe null
        loaded.loadedCargo.shouldBeEmpty()
        ships.getAllShips().single().isIncoming shouldBe true
        aboardRowsFor(saltyWhisker.id) shouldBe 3
    }

    @Test
    fun `a ship with Cargo aboard that is not Incoming round-trips as such`() {
        val refused = Ship(
            name = "Refused Rover", catainId = CatainId(seededCatainId()), catainName = "Furry Jones",
            cargoAboard = listOf(rum()), incoming = false,
        )
        ships.saveNewShip(InitialShipInformation.fromShip(refused))
        flushAndClear()

        val loaded = ships.getShipDetails(refused.id)!!
        loaded.isIncoming shouldBe false
        loaded.cargoAboard.map { it.name } shouldBe listOf("Rum")
    }

    @Test
    fun `a ship row written without the flag is not Incoming and has no Cargo aboard`() {
        val catainRowId = jdbcTemplate.queryForObject("SELECT id FROM catains ORDER BY id LIMIT 1", Long::class.java)
        val shipId = UUID.randomUUID()
        jdbcTemplate.update(
            "INSERT INTO ships (id, ship_id, ship_name, catain_id) VALUES (nextval('ships_seq'), ?, 'Old Timer', ?)",
            shipId, catainRowId,
        )

        val loaded = ships.getShipDetails(ShipId(shipId))!!
        loaded.isIncoming shouldBe false
        loaded.cargoAboard.shouldBeEmpty()
    }

    @Test
    fun `a rename that carries the Cargo aboard keeps it and the Incoming flag`() {
        val saltyWhisker = anIncomingShip("Salty Whisker", listOf(rum(), sugar()))
        flushAndClear()

        val loaded = ships.getShipDetails(saltyWhisker.id)!!
        loaded.shipName = "Salty Whiskers"
        ships.saveNewShip(InitialShipInformation.fromShip(loaded))
        flushAndClear()

        val renamed = ships.getShipDetails(saltyWhisker.id)!!
        renamed.shipName shouldBe "Salty Whiskers"
        renamed.isIncoming shouldBe true
        renamed.cargoAboard.map { it.name } shouldBe listOf("Rum", "Sugar")
        aboardRowsFor(saltyWhisker.id) shouldBe 2
    }

    @Test
    fun `a ship that arrives again replaces its Cargo aboard`() {
        val saltyWhisker = anIncomingShip("Salty Whisker", listOf(rum(), rum()))
        ships.removeFromFleet(saltyWhisker.id)
        flushAndClear()

        ships.saveNewShip(
            InitialShipInformation.fromShip(
                Ship(
                    id = saltyWhisker.id, name = "Salty Whisker", catainId = saltyWhisker.catainId,
                    catainName = saltyWhisker.catainName, cargoAboard = listOf(sugar()), incoming = true,
                )
            )
        )
        flushAndClear()

        ships.getShipDetails(saltyWhisker.id)!!.cargoAboard.map { it.name } shouldBe listOf("Sugar")
        aboardRowsFor(saltyWhisker.id) shouldBe 1
        rowsFor(saltyWhisker.id) shouldBe 1
    }

    @Test
    fun `saveNewShip with an unknown Cargo aboard fails`() {
        val ghost = Cargo(CargoId(UUID.randomUUID()), "Ghost Cargo", 1.0F)
        val ship = Ship(
            name = "Salty Whisker", catainId = CatainId(seededCatainId()), catainName = "Furry Jones",
            cargoAboard = listOf(ghost), incoming = true,
        )

        shouldThrow<jakarta.persistence.EntityNotFoundException> {
            ships.saveNewShip(InitialShipInformation.fromShip(ship))
        }
    }

    @Test
    fun `deleting an Incoming Ship removes it with its Cargo aboard`() {
        val saltyWhisker = anIncomingShip("Salty Whisker", listOf(rum(), sugar()))
        flushAndClear()

        val deleted = ships.delete(saltyWhisker.id)
        flushAndClear()

        deleted shouldBe true
        rowsFor(saltyWhisker.id) shouldBe 0
        jdbcTemplate.queryForObject("SELECT count(*) FROM ships_cargos_aboard", Int::class.java) shouldBe 0
    }

    private fun anIncomingShip(name: String, cargoAboard: List<Cargo>): Ship {
        val ship = Ship(
            name = name,
            catainId = CatainId(seededCatainId()),
            catainName = "Furry Jones",
            arrivedFrom = HarborName("Tortuga"),
            cargoAboard = cargoAboard,
            incoming = true,
        )
        ships.saveNewShip(InitialShipInformation.fromShip(ship))
        entityManager.flush()
        return ship
    }

    private fun rum() = seededCargo("Rum")

    private fun sugar() = seededCargo("Sugar")

    private fun seededCargo(name: String): Cargo = jdbcTemplate.queryForObject(
        "SELECT cargo_id, cargo_weight FROM cargos WHERE cargo_name = ?",
        { rs, _ -> Cargo(CargoId(rs.getObject("cargo_id", UUID::class.java)), name, rs.getFloat("cargo_weight")) },
        name,
    )!!

    private fun aboardRowsFor(shipId: ShipId): Int = jdbcTemplate.queryForObject(
        "SELECT count(*) FROM ships_cargos_aboard a JOIN ships s ON a.ship_id = s.id WHERE s.ship_id = ?",
        Int::class.java,
        shipId.id,
    )!!

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
