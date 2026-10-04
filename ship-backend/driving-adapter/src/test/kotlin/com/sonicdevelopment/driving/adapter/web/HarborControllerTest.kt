package com.sonicdevelopment.driving.adapter.web

import com.ninjasquad.springmockk.MockkBean
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.ports.driving.harbor.HarborInformationPort
import com.sonicdevelopment.domain.ports.driving.harbor.KnownHarborsDTO
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(HarborController::class)
class HarborControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var harborInformationPort: HarborInformationPort

    @Test
    fun `GET web harbors returns the Harbor Name and the Known Harbors`() {
        every { harborInformationPort.getKnownHarbors() } returns KnownHarborsDTO(
            harborName = HarborName("Tortuga"),
            knownHarbors = listOf(HarborName("Nassau"), HarborName("Port Royal"))
        )

        mockMvc.get("/web/harbors").andExpect {
            status { isOk() }
            content { json("""{"harborName":"Tortuga","knownHarbors":["Nassau","Port Royal"]}""", true) }
        }
    }
}
