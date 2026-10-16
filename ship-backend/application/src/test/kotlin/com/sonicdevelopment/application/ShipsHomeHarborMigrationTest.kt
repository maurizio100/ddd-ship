package com.sonicdevelopment.application

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.FlywayException
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DriverManagerDataSource
import java.util.*
import javax.sql.DataSource

/**
 * V17 gives every existing ship its Home Harbor: this Harbor (the `harbor_name` placeholder) for a ship
 * registered here, the Harbor it came from for an arrived ship. The Harbor Name contains an apostrophe, which
 * the dollar-quoting in the migration must survive.
 */
@DbTest
class ShipsHomeHarborMigrationTest {

    private val postgres = PostgresTestcontainer().postgresContainer().also { it.start() }

    @Test
    fun `existing ships get this Harbor or the Harbor they came from as Home Harbor`() {
        val dataSource = aHarborDatabaseBeforeTheHomeHarbor()
        val database = JdbcTemplate(dataSource)
        val registered = UUID.randomUUID()
        val arrived = UUID.randomUUID()
        database.update(
            "INSERT INTO ships (id, ship_id, ship_name, catain_id, ship_arrived_from) VALUES (1, ?, 'Black Pearl', 1, NULL)",
            registered
        )
        database.update(
            "INSERT INTO ships (id, ship_id, ship_name, catain_id, ship_arrived_from) VALUES (2, ?, 'Salty Whisker', 1, 'Tortuga')",
            arrived
        )

        Flyway.configure().dataSource(dataSource).locations("classpath:db/migration")
            .placeholders(mapOf("harbor_name" to "Devil's Cove")).load().migrate()

        database.homeHarborOf(registered) shouldBe "Devil's Cove"
        database.homeHarborOf(arrived) shouldBe "Tortuga"
        database.queryForObject(
            "SELECT is_nullable FROM information_schema.columns WHERE table_name = 'ships' AND column_name = 'ship_home_harbor'",
            String::class.java
        ) shouldBe "NO"
    }

    @Test
    fun `without a Harbor Name the migration fails rather than give registered ships an empty Home Harbor`() {
        val dataSource = aHarborDatabaseBeforeTheHomeHarbor()
        JdbcTemplate(dataSource).update(
            "INSERT INTO ships (id, ship_id, ship_name, catain_id) VALUES (1, ?, 'Black Pearl', 1)", UUID.randomUUID()
        )

        val failure = shouldThrow<FlywayException> {
            Flyway.configure().dataSource(dataSource).locations("classpath:db/migration")
                .placeholders(mapOf("harbor_name" to "")).load().migrate()
        }

        failure.message shouldContain "ship_home_harbor"
    }

    private fun JdbcTemplate.homeHarborOf(shipId: UUID): String? =
        queryForObject("SELECT ship_home_harbor FROM ships WHERE ship_id = ?", String::class.java, shipId)

    /** A new database migrated up to V16, the schema before ships had a Home Harbor. */
    private fun aHarborDatabaseBeforeTheHomeHarbor(): DataSource {
        val databaseName = "harbor_" + UUID.randomUUID().toString().replace("-", "")
        JdbcTemplate(DriverManagerDataSource(postgres.jdbcUrl, postgres.username, postgres.password))
            .execute("CREATE DATABASE $databaseName")
        val dataSource = DriverManagerDataSource(
            postgres.jdbcUrl.replaceAfterLast("/", databaseName), postgres.username, postgres.password
        )
        Flyway.configure().dataSource(dataSource).locations("classpath:db/migration")
            .placeholders(mapOf("harbor_name" to "Devil's Cove")).target("16").load().migrate()
        return dataSource
    }
}
