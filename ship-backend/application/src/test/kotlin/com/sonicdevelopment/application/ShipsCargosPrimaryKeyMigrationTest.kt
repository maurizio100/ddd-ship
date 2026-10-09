package com.sonicdevelopment.application

import io.kotest.matchers.shouldBe
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DriverManagerDataSource
import java.util.*
import javax.sql.DataSource

/**
 * Debezium created `dbz_publication` FOR ALL TABLES on existing Harbor databases, and some of them were
 * patched by hand with `REPLICA IDENTITY FULL` on `ships_cargos`. Giving `ships_cargos` its primary key
 * must migrate both kinds of database and leave them like a fresh one.
 */
@DbTest
class ShipsCargosPrimaryKeyMigrationTest {

    private val postgres = PostgresTestcontainer().postgresContainer().also { it.start() }

    @Test
    fun `a Harbor database patched with REPLICA IDENTITY FULL under an all-tables publication migrates`() {
        val dataSource = aHarborDatabaseBeforeTheShipsCargosPrimaryKey()
        val database = JdbcTemplate(dataSource)
        database.execute("ALTER TABLE ships_cargos REPLICA IDENTITY FULL")
        database.execute("CREATE PUBLICATION dbz_publication FOR ALL TABLES")

        migrateToLatest(dataSource)

        database.shouldHaveShipsCargosPrimaryKeyAsReplicaIdentity()
    }

    @Test
    fun `an unpatched Harbor database under an all-tables publication migrates`() {
        val dataSource = aHarborDatabaseBeforeTheShipsCargosPrimaryKey()
        val database = JdbcTemplate(dataSource)
        database.execute("CREATE PUBLICATION dbz_publication FOR ALL TABLES")

        migrateToLatest(dataSource)

        database.shouldHaveShipsCargosPrimaryKeyAsReplicaIdentity()
    }

    private fun JdbcTemplate.shouldHaveShipsCargosPrimaryKeyAsReplicaIdentity() {
        queryForObject(
            "SELECT count(*) FROM pg_constraint WHERE conname = 'pk_ships_cargos' AND contype = 'p'",
            Long::class.java
        ) shouldBe 1L
        queryForObject(
            "SELECT relreplident::text FROM pg_class WHERE relname = 'ships_cargos'",
            String::class.java
        ) shouldBe "d"
    }

    private fun migrateToLatest(dataSource: DataSource) {
        Flyway.configure().dataSource(dataSource).locations("classpath:db/migration")
            .placeholders(mapOf("harbor_name" to "Test Harbor")).load().migrate()
    }

    /** A new database migrated up to V11, the schema the live Harbors had before `ships_cargos` got its key. */
    private fun aHarborDatabaseBeforeTheShipsCargosPrimaryKey(): DataSource {
        val databaseName = "harbor_" + UUID.randomUUID().toString().replace("-", "")
        JdbcTemplate(DriverManagerDataSource(postgres.jdbcUrl, postgres.username, postgres.password))
            .execute("CREATE DATABASE $databaseName")
        val dataSource = DriverManagerDataSource(
            postgres.jdbcUrl.replaceAfterLast("/", databaseName), postgres.username, postgres.password
        )
        Flyway.configure().dataSource(dataSource).locations("classpath:db/migration")
            .placeholders(mapOf("harbor_name" to "Test Harbor")).target("11").load().migrate()
        return dataSource
    }
}
