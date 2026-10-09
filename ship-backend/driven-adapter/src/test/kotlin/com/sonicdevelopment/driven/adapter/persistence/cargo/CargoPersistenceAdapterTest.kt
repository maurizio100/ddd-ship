package com.sonicdevelopment.driven.adapter.persistence.cargo

import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.values.CatainId
import com.sonicdevelopment.domain.ports.driven.CargoPersistencePort.CargoLoadInformation
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort.InitialShipInformation
import com.sonicdevelopment.driven.adapter.PostgresTestcontainer
import com.sonicdevelopment.driven.adapter.persistence.ship.ShipRepositoryAdapter
import com.sonicdevelopment.driven.adapter.persistence.shipping.ShippingRepositoryAdapter
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
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
import java.util.*

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(
    PostgresTestcontainer::class,
    CargoPersistenceAdapter::class,
    CargoQueryAdapter::class,
    ShipRepositoryAdapter::class,
    ShippingRepositoryAdapter::class
)
class CargoPersistenceAdapterTest {

    @Autowired
    lateinit var cargoLoads: CargoPersistenceAdapter

    @Autowired
    lateinit var cargos: CargoQueryAdapter

    @Autowired
    lateinit var ships: ShipRepositoryAdapter

    @Autowired
    lateinit var shippings: ShippingRepositoryAdapter

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    lateinit var entityManager: TestEntityManager

    /**
     * Debezium's default publication covers all tables. The publication is created inside the test
     * transaction, so it is rolled back with it and never leaks into other tests.
     */
    @BeforeEach
    fun truncateMutableTablesAndPublishAllTables() {
        jdbcTemplate.execute("TRUNCATE TABLE ships_cargos, shippings, ships")
        jdbcTemplate.execute("DROP PUBLICATION IF EXISTS all_tables_regression")
        jdbcTemplate.execute("CREATE PUBLICATION all_tables_regression FOR ALL TABLES")
    }

    @Test
    fun `Loaded Cargo can be loaded and unloaded while a publication covers all tables`() {
        val ship = aShipBeingPrepared()
        val (first, second) = cargos.findAllCargo().sortedBy { it.weight }.take(2)

        ship.addCargo(first)
        cargoLoads.updateCargoLoad(CargoLoadInformation.fromShip(ship))
        entityManager.flush()
        ship.addCargo(second)
        cargoLoads.updateCargoLoad(CargoLoadInformation.fromShip(ship))
        entityManager.flush()

        ship.removeCargo(first)
        cargoLoads.updateCargoLoad(CargoLoadInformation.fromShip(ship))
        entityManager.flush()

        jdbcTemplate.queryForObject(
            """
            SELECT count(*) FROM ships_cargos sc
            JOIN shippings s ON s.id = sc.ship_id
            WHERE s.shipping_id = ?
            """.trimIndent(),
            Long::class.java,
            ship.activeShipping!!.id.id
        ) shouldBe 1L
    }

    @Test
    fun `ships_cargos has a primary key on id`() {
        val primaryKeyColumns = jdbcTemplate.queryForList(
            """
            SELECT a.attname FROM pg_index i
            JOIN pg_attribute a ON a.attrelid = i.indrelid AND a.attnum = ANY(i.indkey)
            WHERE i.indrelid = 'ships_cargos'::regclass AND i.indisprimary
            """.trimIndent(),
            String::class.java
        )

        primaryKeyColumns shouldContainExactlyInAnyOrder listOf("id")
    }

    @Test
    fun `loading the same Cargo twice persists two rows in ships_cargos, each with a distinct database-generated id`() {
        val ship = aShipBeingPrepared()
        val cargo = cargos.findAllCargo().first()

        ship.addCargo(cargo)
        cargoLoads.updateCargoLoad(CargoLoadInformation.fromShip(ship))
        entityManager.flush()
        ship.addCargo(cargo)
        cargoLoads.updateCargoLoad(CargoLoadInformation.fromShip(ship))
        entityManager.flush()

        val ids = jdbcTemplate.queryForList(
            """
            SELECT sc.id FROM ships_cargos sc
            JOIN shippings s ON s.id = sc.ship_id
            WHERE s.shipping_id = ?
            """.trimIndent(),
            Long::class.java,
            ship.activeShipping!!.id.id
        )

        ids.size shouldBe 2
        ids.toSet().size shouldBe 2
        ids.forEach { it shouldNotBe null }
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
