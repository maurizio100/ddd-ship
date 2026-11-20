package com.sonicdevelopment.driving.adapter.web

import com.sonicdevelopment.domain.model.values.Accessory
import com.sonicdevelopment.domain.model.values.CoatPattern
import com.sonicdevelopment.domain.model.values.EyeColour
import com.sonicdevelopment.domain.model.values.FurColour
import com.sonicdevelopment.domain.model.values.Portrait
import com.ninjasquad.springmockk.MockkBean
import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.domain.ports.driving.harbor.HarborInformationPort
import com.sonicdevelopment.domain.ports.driving.harbor.RecruitDTO
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.util.UUID

@WebMvcTest(RecruitController::class)
class RecruitControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var harborInformationPort: HarborInformationPort

    @Test
    fun `GET web recruits returns the Recruit Pool as JSON`() {
        val id = UUID.fromString("11111111-1111-1111-1111-111111111111")
        every { harborInformationPort.getRecruitPool() } returns listOf(RecruitDTO(id, "Mittens", "Fearless", Money.of("50.00"), Portrait(FurColour.GINGER, CoatPattern.PLAIN, EyeColour.GREEN, Accessory.NONE)))

        mockMvc.get("/web/recruits").andExpect {
            status { isOk() }
            content { json("""[{"id":"$id","name":"Mittens","trait":"Fearless","hiringFee":"50.00","portrait":{"fur":"ginger","coat":"plain","eyes":"green","accessory":"none"}}]""", true) }
        }
    }
}
