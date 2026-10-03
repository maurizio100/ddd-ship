package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.ports.driven.KnownHarborRepositoryPort
import com.sonicdevelopment.domain.ports.driving.harbor.KnownHarborsDTO
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class HarborInformationServiceTest {

    @Test
    fun `returns the current Harbor Name and the Known Harbors`() {
        val knownHarbors = mockk<KnownHarborRepositoryPort>()
        every { knownHarbors.getKnownHarbors() } returns listOf(HarborName("Nassau"), HarborName("Port Royal"))
        val service = HarborInformationService(HarborName("Tortuga"), knownHarbors)

        val result = service.getKnownHarbors()

        result shouldBe KnownHarborsDTO(
            harborName = HarborName("Tortuga"),
            knownHarbors = listOf(HarborName("Nassau"), HarborName("Port Royal"))
        )
    }
}
