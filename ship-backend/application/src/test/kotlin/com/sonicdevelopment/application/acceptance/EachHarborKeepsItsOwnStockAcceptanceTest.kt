package com.sonicdevelopment.application.acceptance

import com.sonicdevelopment.application.DbTest
import com.sonicdevelopment.application.PostgresTestcontainer
import com.sonicdevelopment.application.acceptance.fixtures.STARTING_STOCK
import com.sonicdevelopment.application.acceptance.fixtures.aShipBeingPrepared
import com.sonicdevelopment.application.acceptance.fixtures.cargoIdOf
import com.sonicdevelopment.application.acceptance.fixtures.givenStockOf
import com.sonicdevelopment.application.acceptance.fixtures.resetStockToStartingStock
import com.sonicdevelopment.application.acceptance.fixtures.truncateMutableTables
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.test.context.TestPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import java.util.*

/**
 * Feature: Each Harbor keeps its own Stock.
 *
 * The Stock is observed the way the User sees it: through the Available Cargo (`GET /web/cargos`).
 */
@DbTest
class EachHarborKeepsItsOwnStockAcceptanceTest {

    @Nested
    inner class ANewHarbor : AHarbor() {

        @Autowired
        lateinit var postgres: PostgreSQLContainer<*>

        @Test
        fun `A new Harbor starts with its Starting Stock`() {
            // When a Harbor opens for the first time
            val harborDatabase = aHarborOpensForTheFirstTime()

            // Then its Stock equals its Starting Stock
            harborDatabase.queryForList(
                "SELECT c.cargo_name, s.stock_quantity FROM cargos c LEFT JOIN stocks s ON s.cargo_id = c.id"
            ).forEach { it["stock_quantity"] shouldBe STARTING_STOCK }
            harborDatabase.queryForObject("SELECT count(*) FROM stocks", Int::class.java) shouldBe
                harborDatabase.queryForObject("SELECT count(*) FROM cargos", Int::class.java)
        }

        @Test
        fun `a restarted Harbor keeps its Stock`() {
            // Given a Harbor whose Stock has changed since it opened
            val harborDatabase = aHarborOpensForTheFirstTime()
            harborDatabase.givenStockOf("Rum", 1)

            // When the Harbor opens again
            migrate(harborDatabase.dataSource as DriverManagerDataSource)

            // Then its Stock is unchanged: the Starting Stock is never applied again
            harborDatabase.queryForObject(
                "SELECT s.stock_quantity FROM stocks s JOIN cargos c ON s.cargo_id = c.id WHERE c.cargo_name = 'Rum'",
                Int::class.java
            ) shouldBe 1
        }

        /** Creates an empty database for a new Harbor and migrates it, as the Harbor's first start does. */
        private fun aHarborOpensForTheFirstTime(): JdbcTemplate {
            val databaseName = "harbor_" + UUID.randomUUID().toString().replace("-", "")
            jdbcTemplate.execute("CREATE DATABASE $databaseName")
            val dataSource = DriverManagerDataSource(
                postgres.jdbcUrl.replaceAfterLast("/", databaseName), postgres.username, postgres.password
            )
            migrate(dataSource)
            return JdbcTemplate(dataSource)
        }

        private fun migrate(dataSource: DriverManagerDataSource) {
            Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load().migrate()
        }
    }

    @Nested
    inner class AtTortuga : AHarbor() {

        @Test
        fun `Loading Cargo takes it out of the Stock`() {
            // Given the Stock of "Tortuga" holds 2 "Rum"
            jdbcTemplate.givenStockOf("Rum", 2)
            // And a ship at "Tortuga" is being prepared
            val shipId = restTemplate.aShipBeingPrepared(jdbcTemplate)

            // When the User loads "Rum" onto the ship
            val load = load(shipId, "Rum")

            // Then the Stock of "Tortuga" holds 1 "Rum"
            load.statusCode shouldBe HttpStatus.OK
            stockOf("Rum") shouldBe 1
        }

        @Test
        fun `Unloading Cargo while preparing puts it back into the Stock`() {
            // Given the Stock of "Tortuga" holds 1 "Rum"
            // And a ship at "Tortuga" being prepared has "Rum" loaded
            jdbcTemplate.givenStockOf("Rum", 2)
            val shipId = restTemplate.aShipBeingPrepared(jdbcTemplate)
            load(shipId, "Rum").statusCode shouldBe HttpStatus.OK
            stockOf("Rum") shouldBe 1

            // When the User unloads "Rum" from the ship
            val unload = unload(shipId, "Rum")

            // Then the Stock of "Tortuga" holds 2 "Rum"
            unload.statusCode shouldBe HttpStatus.OK
            stockOf("Rum") shouldBe 2
        }

        @Test
        fun `Cargo that is out of Stock cannot be loaded`() {
            // Given the Stock of "Tortuga" holds no "Silk"
            jdbcTemplate.givenStockOf("Silk", 0)

            // Then "Silk" is not among the Available Cargo at "Tortuga"
            availableCargo().map { it["name"] } shouldNotContain "Silk"
            // And loading "Silk" onto a ship at "Tortuga" is rejected
            val shipId = restTemplate.aShipBeingPrepared(jdbcTemplate)
            val load = load(shipId, "Silk")
            load.statusCode shouldBe HttpStatus.CONFLICT
            load.headers.contentType?.isCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON) shouldBe true
            load.body!!["detail"] shouldBe "Silk is out of Stock"
            loadedCargo(shipId) shouldNotContain "Silk"
        }

        @Test
        fun `A rejected load leaves the Stock unchanged`() {
            // Given the Stock of "Tortuga" holds 2 "Rum"
            jdbcTemplate.givenStockOf("Rum", 2)
            // And a ship at "Tortuga" being prepared has a Current Weight of 14.0
            val shipId = restTemplate.aShipBeingPrepared(jdbcTemplate)
            listOf("Tobacco", "Silk", "Ale", "Wheat", "Planks", "Paprika").forEach {
                load(shipId, it).statusCode shouldBe HttpStatus.OK
            }
            currentWeight(shipId) shouldBe 14.0

            // When the User loads "Rum" onto the ship
            val load = load(shipId, "Rum")

            // Then the load is rejected because the ship would exceed its Max Weight
            load.statusCode shouldBe HttpStatus.CONFLICT
            (load.body!!["detail"] as String) shouldContain "Max Weight"
            // And the Stock of "Tortuga" still holds 2 "Rum"
            stockOf("Rum") shouldBe 2
            loadedCargo(shipId) shouldContainExactlyInAnyOrder
                listOf("Tobacco", "Silk", "Ale", "Wheat", "Planks", "Paprika")
        }
    }

    /** The Harbor "Tortuga": the app against the shared Postgres, with Kafka unreachable. */
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @Import(PostgresTestcontainer::class)
    @TestPropertySource(
        properties = [
            "harbor.name=Tortuga",
            "spring.kafka.bootstrap-servers=localhost:1",
            "spring.kafka.listener.auto-startup=false",
        ]
    )
    abstract class AHarbor {

        @Autowired
        lateinit var restTemplate: TestRestTemplate

        @Autowired
        lateinit var jdbcTemplate: JdbcTemplate

        @BeforeEach
        fun resetHarbor() {
            jdbcTemplate.truncateMutableTables()
            jdbcTemplate.resetStockToStartingStock()
        }

        fun load(shipId: UUID, cargoName: String) =
            restTemplate.postForEntity(
                "/web/ships/$shipId/cargos", mapOf("cargoId" to jdbcTemplate.cargoIdOf(cargoName)), Map::class.java
            )

        fun unload(shipId: UUID, cargoName: String) =
            restTemplate.exchange(
                "/web/ships/$shipId/cargos/${jdbcTemplate.cargoIdOf(cargoName)}", HttpMethod.DELETE, null, Map::class.java
            )

        fun availableCargo(): List<Map<*, *>> {
            val response = restTemplate.getForEntity("/web/cargos", List::class.java)
            response.statusCode shouldBe HttpStatus.OK
            return response.body!!.map { it as Map<*, *> }
        }

        /** The Stock of [cargoName] as the User sees it among the Available Cargo; 0 when it isn't offered. */
        fun stockOf(cargoName: String): Int =
            (availableCargo().singleOrNull { it["name"] == cargoName }?.get("stock") as Int?) ?: 0

        fun ship(shipId: UUID): Map<*, *> {
            val response = restTemplate.getForEntity("/web/ships/$shipId", Map::class.java)
            response.statusCode shouldBe HttpStatus.OK
            return response.body!!
        }

        fun loadedCargo(shipId: UUID): List<String> =
            (ship(shipId)["cargo"] as List<*>).map { (it as Map<*, *>)["name"] as String }

        fun currentWeight(shipId: UUID): Double = (ship(shipId)["weight"] as Number).toDouble()
    }
}
