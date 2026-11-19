package com.sonicdevelopment.driving.adapter.web

import com.ninjasquad.springmockk.MockkBean
import com.sonicdevelopment.domain.exception.SavingsDoNotCoverException
import com.sonicdevelopment.domain.exception.ShipNotImprisonedException
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.ports.driving.harbor.HarborInformationPort
import com.sonicdevelopment.domain.ports.driving.harbor.ImprisonedShipDTO
import com.sonicdevelopment.domain.ports.driving.shipping.FinePaymentDTO
import com.sonicdevelopment.domain.ports.driving.shipping.ImprisonmentManagementPort
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.util.*

@WebMvcTest(ImprisonedShipController::class)
class ImprisonedShipControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var harborInformationPort: HarborInformationPort

    @MockkBean
    lateinit var imprisonmentManagementPort: ImprisonmentManagementPort

    @Test
    fun `GET web imprisoned-ships lists each Imprisoned Ship with its Fine as a decimal string and whether the Savings cover it`() {
        val saltyWhisker = UUID.randomUUID()
        val seaBiscuit = UUID.randomUUID()
        every { harborInformationPort.getImprisonedShips() } returns listOf(
            ImprisonedShipDTO(ShipId(saltyWhisker), "Salty Whisker", "Furry Jones", HarborName("Tortuga"), Money.of("50.00"), true),
            ImprisonedShipDTO(ShipId(seaBiscuit), "Sea Biscuit", "Whiskers McGee", HarborName("Tortuga"), Money.of("50.00"), false),
        )

        mockMvc.get("/web/imprisoned-ships").andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(2) }
            jsonPath("$[0].shipId") { value(saltyWhisker.toString()) }
            jsonPath("$[0].name") { value("Salty Whisker") }
            jsonPath("$[0].catain") { value("Furry Jones") }
            jsonPath("$[0].homeHarbor") { value("Tortuga") }
            jsonPath("$[0].fine") { isString() }
            jsonPath("$[0].fine") { value("50.00") }
            jsonPath("$[0].savingsCoverFine") { value(true) }
            jsonPath("$[1].savingsCoverFine") { value(false) }
        }
    }

    @Test
    fun `GET web imprisoned-ships is empty while no ship is imprisoned here`() {
        every { harborInformationPort.getImprisonedShips() } returns emptyList()

        mockMvc.get("/web/imprisoned-ships").andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(0) }
        }
    }

    @Test
    fun `POST fine-payment frees the ship and answers 200 with the Fine and the Savings after`() {
        val saltyWhisker = UUID.randomUUID()
        every { imprisonmentManagementPort.payFine(ShipId(saltyWhisker)) } returns
            FinePaymentDTO(ShipId(saltyWhisker), "Salty Whisker", Money.of("50.00"), Money.of("950.00"))

        mockMvc.post("/web/imprisoned-ships/$saltyWhisker/fine-payment").andExpect {
            status { isOk() }
            jsonPath("$.shipId") { value(saltyWhisker.toString()) }
            jsonPath("$.name") { value("Salty Whisker") }
            jsonPath("$.fine") { value("50.00") }
            jsonPath("$.savings") { isString() }
            jsonPath("$.savings") { value("950.00") }
        }
    }

    @Test
    fun `POST fine-payment for a ship not in the fleet answers 404 as Problem Details`() {
        every { imprisonmentManagementPort.payFine(any()) } returns null

        mockMvc.post("/web/imprisoned-ships/${UUID.randomUUID()}/fine-payment").andExpect {
            status { isNotFound() }
            content { contentTypeCompatibleWith("application/problem+json") }
            jsonPath("$.status") { value(404) }
        }
    }

    @Test
    fun `POST fine-payment the Savings cannot cover answers 409 with the Fine`() {
        every { imprisonmentManagementPort.payFine(any()) } throws
            SavingsDoNotCoverException(Money.of("50.00"), "The Savings do not cover the Fine of 50.00 $")

        mockMvc.post("/web/imprisoned-ships/${UUID.randomUUID()}/fine-payment").andExpect {
            status { isConflict() }
            content { contentTypeCompatibleWith("application/problem+json") }
            jsonPath("$.title") { value("Savings do not cover") }
            jsonPath("$.detail") { value("The Savings do not cover the Fine of 50.00 $") }
        }
    }

    @Test
    fun `POST fine-payment for a ship that is not imprisoned answers 409`() {
        every { imprisonmentManagementPort.payFine(any()) } throws ShipNotImprisonedException("Sea Biscuit is not imprisoned")

        mockMvc.post("/web/imprisoned-ships/${UUID.randomUUID()}/fine-payment").andExpect {
            status { isConflict() }
            content { contentTypeCompatibleWith("application/problem+json") }
            jsonPath("$.title") { value("Ship not imprisoned") }
            jsonPath("$.detail") { value("Sea Biscuit is not imprisoned") }
        }
    }
}
