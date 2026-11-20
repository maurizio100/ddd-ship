package com.sonicdevelopment.domain.model.values

import com.sonicdevelopment.domain.model.CrewMember
import com.sonicdevelopment.domain.model.Recruit
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.util.UUID

class PortraitTest {

    private val many = (0 until 2000).map { UUID.nameUUIDFromBytes("cat-$it".toByteArray()) }

    @Test
    fun `a portrait depends only on the UUID`() {
        val id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")

        Portrait.of(id) shouldBe Portrait.of(UUID.fromString(id.toString()))
    }

    @Test
    fun `every fur, coat, eye colour and accessory occurs over many UUIDs`() {
        val portraits = many.map { Portrait.of(it) }

        portraits.map { it.fur }.distinct() shouldContainAll FurColour.entries
        portraits.map { it.coat }.distinct() shouldContainAll CoatPattern.entries
        portraits.map { it.eyes }.distinct() shouldContainAll EyeColour.entries
        portraits.map { it.accessory }.distinct() shouldContainAll Accessory.entries
    }

    @Test
    fun `different UUIDs give more than a handful of different portraits`() {
        (many.map { Portrait.of(it) }.distinct().size > 20) shouldBe true
    }

    @Test
    fun `a Recruit and the Crew Member hired from it have the same portrait`() {
        val recruit = Recruit(RecruitId(many[7]), "Mittens", "Fearless")

        recruit.portrait shouldBe Portrait.of(many[7])
        CrewMember.hired(recruit).portrait shouldBe recruit.portrait
    }
}
