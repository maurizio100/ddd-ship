package com.sonicdevelopment.application

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DriverManagerDataSource
import java.math.BigDecimal
import java.util.*

/**
 * Every Harbor has its own database, and an arriving ship names its Catain and Loaded Cargo by id, so
 * the seeded reference data must carry the same business ids at every Harbor.
 */
@DbTest
class ReferenceDataIdsTest {

    private val postgres = PostgresTestcontainer().postgresContainer().also { it.start() }

    @Test
    fun `every Harbor seeds the same Cargo and Catain ids`() {
        val tortuga = aHarborDatabase()
        val portRoyal = aHarborDatabase()

        val cargoIds = "SELECT cargo_name, cargo_id FROM cargos ORDER BY id"
        val catainIds = "SELECT catain_name, catain_id FROM catains ORDER BY id"
        tortuga.queryForList(cargoIds) shouldHaveSize 14
        tortuga.queryForList(catainIds) shouldHaveSize 5
        portRoyal.queryForList(cargoIds) shouldBe tortuga.queryForList(cargoIds)
        portRoyal.queryForList(catainIds) shouldBe tortuga.queryForList(catainIds)
    }

    @Test
    fun `every Harbor seeds the Starting Savings but no Prices`() {
        listOf(aHarborDatabase(), aHarborDatabase()).forEach { harbor ->
            harbor.queryForList("SELECT savings_amount FROM savings", BigDecimal::class.java) shouldBe
                listOf(BigDecimal("1000.00"))
            harbor.queryForObject("SELECT count(*) FROM prices", Int::class.java) shouldBe 0
        }
    }

    /** A new, empty database migrated by Flyway, as a Harbor's first start does. */
    private fun aHarborDatabase(): JdbcTemplate {
        val databaseName = "harbor_" + UUID.randomUUID().toString().replace("-", "")
        JdbcTemplate(DriverManagerDataSource(postgres.jdbcUrl, postgres.username, postgres.password))
            .execute("CREATE DATABASE $databaseName")
        val dataSource = DriverManagerDataSource(
            postgres.jdbcUrl.replaceAfterLast("/", databaseName), postgres.username, postgres.password
        )
        Flyway.configure().dataSource(dataSource).locations("classpath:db/migration")
            .placeholders(mapOf("harbor_name" to "Test Harbor")).load().migrate()
        return JdbcTemplate(dataSource)
    }
}
