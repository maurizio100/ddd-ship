package com.sonicdevelopment.driven.adapter.persistence.recruit

import com.sonicdevelopment.domain.model.Recruit
import com.sonicdevelopment.domain.model.values.RecruitId
import com.sonicdevelopment.driven.adapter.DbTest
import com.sonicdevelopment.driven.adapter.PostgresTestcontainer
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import java.util.*

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgresTestcontainer::class, RecruitPoolRepositoryAdapter::class)
@DbTest
class RecruitPoolRepositoryAdapterTest {

    @Autowired
    lateinit var recruits: RecruitPoolRepositoryAdapter

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun noRecruits() {
        jdbcTemplate.execute("DELETE FROM recruits")
    }

    private fun recruit(name: String) = Recruit(RecruitId(UUID.randomUUID()), name, "Calm")

    @Test
    fun `an empty pool starts at ordinal 0`() {
        recruits.getRecruits() shouldBe emptyList()
        recruits.nextOrdinal() shouldBe 0
    }

    @Test
    fun `keeps the first Recruit per ordinal and reports false for a second one`() {
        val first = recruit("Mittens")

        recruits.rememberRecruit(0, first) shouldBe true
        recruits.rememberRecruit(0, recruit("Patch")) shouldBe false

        recruits.getRecruits() shouldBe listOf(first)
    }

    @Test
    fun `lists the Recruits by ordinal and the next ordinal follows the highest`() {
        val a = recruit("A")
        val b = recruit("B")
        recruits.rememberRecruit(7, b)
        recruits.rememberRecruit(2, a)

        recruits.getRecruits() shouldBe listOf(a, b)
        recruits.nextOrdinal() shouldBe 8
    }

    @Test
    fun `takeRecruit marks an offered Recruit hired once, the pool drops it and nextOrdinal still counts it`() {
        val a = recruit("A")
        val b = recruit("B")
        recruits.rememberRecruit(0, a)
        recruits.rememberRecruit(1, b)

        recruits.takeRecruit(b.id) shouldBe true
        recruits.takeRecruit(b.id) shouldBe false
        recruits.takeRecruit(RecruitId(UUID.randomUUID())) shouldBe false

        recruits.getRecruits() shouldBe listOf(a)
        recruits.nextOrdinal() shouldBe 2
    }
}
