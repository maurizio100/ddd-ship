package com.sonicdevelopment.driven.adapter.persistence.outbox

import com.sonicdevelopment.driven.adapter.DbTest
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.driven.adapter.PostgresTestcontainer
import io.kotest.matchers.collections.shouldHaveSize
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
@Import(PostgresTestcontainer::class, HarborOutboxRepositoryAdapter::class)
@DbTest
class HarborOutboxRepositoryAdapterTest {

    @Autowired
    lateinit var harborOutbox: HarborOutboxRepositoryAdapter

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    lateinit var entityManager: TestEntityManager

    @BeforeEach
    fun truncateOutbox() {
        jdbcTemplate.execute("TRUNCATE TABLE shipping_outbox")
    }

    @Test
    fun `writes harbor-opened keyed by the UUIDv5 of the Harbor Name`() {
        harborOutbox.publishHarborOpened(HarborName("Tortuga"))
        harborOutbox.publishHarborOpened(HarborName("Tortuga"))
        entityManager.flush()

        val rows = jdbcTemplate.queryForList(
            "SELECT message_id, aggregate_type, aggregate_id, event_type, payload FROM shipping_outbox"
        )
        rows shouldHaveSize 2
        rows.forEach { row ->
            row["aggregate_type"] shouldBe "harbor"
            row["event_type"] shouldBe "harbor-opened"
            row["aggregate_id"] shouldBe UUID.fromString("62c95430-dc41-5f98-9511-b4df1303919d")
            row["payload"] shouldBe """{"harborName":"Tortuga"}"""
        }
        rows.map { it["message_id"] }.toSet() shouldHaveSize 2
    }
}
