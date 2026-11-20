package com.sonicdevelopment.domain.service

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class HiringFeeTest {

    @Test
    fun `the refund is half the Hiring Fee`() {
        HiringFee.REFUND * 2 shouldBe HiringFee.AMOUNT
    }
}
