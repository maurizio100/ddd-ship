package com.sonicdevelopment.driving.adapter.web

import com.ninjasquad.springmockk.MockkBean
import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.domain.ports.driving.harbor.HarborInformationPort
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(SavingsController::class)
class SavingsControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var harborInformationPort: HarborInformationPort

    @Test
    fun `GET web savings returns the Savings as a decimal string`() {
        every { harborInformationPort.getSavings() } returns Money.dollars(1000)

        mockMvc.get("/web/savings").andExpect {
            status { isOk() }
            content { json("""{"amount":"1000.00"}""", true) }
            jsonPath("$.amount") { isString() }
        }
    }
}
