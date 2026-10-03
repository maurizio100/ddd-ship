package com.sonicdevelopment.domain.model.values

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class HarborNameTest {

    @Test
    fun `a Harbor Name must not be blank`() {
        shouldThrow<IllegalArgumentException> { HarborName("") }
        shouldThrow<IllegalArgumentException> { HarborName("   ") }
        HarborName("Tortuga").name shouldBe "Tortuga"
    }
}
