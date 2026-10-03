package com.sonicdevelopment.driven.adapter.persistence.harbor

import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.driven.adapter.PostgresTestcontainer
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.IllegalTransactionStateException
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgresTestcontainer::class, KnownHarborRepositoryAdapter::class)
class KnownHarborRepositoryAdapterTest {

    @Autowired
    lateinit var knownHarbors: KnownHarborRepositoryAdapter

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun truncateKnownHarbors() {
        jdbcTemplate.execute("TRUNCATE TABLE known_harbors")
    }

    @Test
    fun `remembers a Harbor`() {
        knownHarbors.rememberHarbor(HarborName("Port Royal"))

        rowsFor("Port Royal") shouldBe 1
    }

    @Test
    fun `remembering a Harbor twice keeps one row`() {
        knownHarbors.rememberHarbor(HarborName("Port Royal"))

        knownHarbors.rememberHarbor(HarborName("Port Royal"))

        rowsFor("Port Royal") shouldBe 1
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun `refuses to run outside a transaction`() {
        shouldThrow<IllegalTransactionStateException> {
            knownHarbors.rememberHarbor(HarborName("Port Royal"))
        }
    }

    @Test
    fun `lists Known Harbors by name`() {
        knownHarbors.rememberHarbor(HarborName("Tortuga"))
        knownHarbors.rememberHarbor(HarborName("Nassau"))
        knownHarbors.rememberHarbor(HarborName("Port Royal"))

        knownHarbors.getKnownHarbors() shouldBe listOf(
            HarborName("Nassau"), HarborName("Port Royal"), HarborName("Tortuga")
        )
    }

    private fun rowsFor(harborName: String): Int =
        jdbcTemplate.queryForObject(
            "SELECT count(*) FROM known_harbors WHERE harbor_name = ?", Int::class.java, harborName
        )!!
}
