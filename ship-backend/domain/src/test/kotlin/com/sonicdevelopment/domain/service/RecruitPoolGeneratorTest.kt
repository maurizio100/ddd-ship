package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.model.values.HarborName
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

class RecruitPoolGeneratorTest {

    private val portRoyal = RecruitPoolGenerator(HarborName("Port Royal"))
    private val tortuga = RecruitPoolGenerator(HarborName("Tortuga"))

    @Test
    fun `the same Harbor and ordinal give the same Recruit`() {
        RecruitPoolGenerator(HarborName("Port Royal")).recruit(3) shouldBe portRoyal.recruit(3)
    }

    @Test
    fun `the first five ordinals are five distinct Recruits with a name and a trait`() {
        val pool = (0 until RECRUIT_POOL_SIZE).map { portRoyal.recruit(it) }

        pool.map { it.name }.distinct() shouldHaveSize 5
        pool.map { it.id }.distinct() shouldHaveSize 5
        pool.forEach { it.name.isNotBlank() shouldBe true; it.trait.isNotBlank() shouldBe true }
    }

    @Test
    fun `later ordinals never repeat an earlier Recruit`() {
        val all = (0 until 100).map { portRoyal.recruit(it) }

        all.map { it.name }.distinct() shouldHaveSize 100
        all.map { it.id }.distinct() shouldHaveSize 100
        portRoyal.recruit(40).name shouldNotBe portRoyal.recruit(0).name
    }

    @Test
    fun `Port Royal and Tortuga offer no Recruit in common`() {
        val offeredAtPortRoyal = (0 until RECRUIT_POOL_SIZE).map { portRoyal.recruit(it) }
        val offeredAtTortuga = (0 until RECRUIT_POOL_SIZE).map { tortuga.recruit(it) }

        offeredAtPortRoyal.map { it.name }.intersect(offeredAtTortuga.map { it.name }.toSet()) shouldBe emptySet()
        (0 until 50).map { portRoyal.recruit(it).id }.intersect((0 until 50).map { tortuga.recruit(it).id }.toSet()) shouldBe emptySet()
    }

    @Test
    fun `Port Royal offers exactly these five Recruits`() {
        (0 until RECRUIT_POOL_SIZE).map { portRoyal.recruit(it).name to portRoyal.recruit(it).trait } shouldBe listOf(
            "Lavender" to "Sleepy", "Gizmo" to "Greedy", "Soot" to "Curious", "Haddock" to "Sticky-pawed",
            "Cobweb" to "Stubborn"
        )
    }
}
