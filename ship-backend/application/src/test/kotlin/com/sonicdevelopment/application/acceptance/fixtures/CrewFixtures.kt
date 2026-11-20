package com.sonicdevelopment.application.acceptance.fixtures

import com.sonicdevelopment.domain.model.Recruit
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.RecruitId
import com.sonicdevelopment.domain.service.RecruitPoolGenerator
import java.util.UUID

/** The id of the Recruit "Mittens" offered by [givenRecruitPool]. */
val MITTENS_ID: UUID = UUID.fromString("00000000-0000-4000-8000-00000000a001")

/**
 * The Recruit Pool of [harbor] offers "Mittens" with the trait "Strong Paws" at ordinal 0 and the generated Recruits
 * of ordinals 1 to 4. Returns the pool in that order.
 */
fun FakeDrivenPorts.givenRecruitPool(harbor: String): List<Recruit> {
    val generator = RecruitPoolGenerator(HarborName(harbor))
    val pool = listOf(Recruit(RecruitId(MITTENS_ID), "Mittens", "Strong Paws")) + (1..4).map { generator.recruit(it) }
    pool.forEachIndexed { ordinal, recruit -> recruits.rememberRecruit(ordinal, recruit) }
    return pool
}
