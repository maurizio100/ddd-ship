package com.sonicdevelopment.driving.adapter.web

import com.sonicdevelopment.domain.model.values.Portrait
import com.ninjasquad.springmockk.MockkBean
import com.sonicdevelopment.domain.exception.DismissRefusedException
import com.sonicdevelopment.domain.exception.HireRefusedException
import com.sonicdevelopment.domain.exception.SavingsDoNotCoverException
import com.sonicdevelopment.domain.model.values.CrewMemberId
import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.domain.model.values.RecruitId
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.ports.driving.harbor.RecruitDTO
import com.sonicdevelopment.domain.ports.driving.ship.CrewManagementPort
import com.sonicdevelopment.domain.ports.driving.ship.CrewMemberDTO
import com.sonicdevelopment.domain.ports.driving.ship.HireDTO
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.post
import java.util.UUID

@WebMvcTest(ShipCrewController::class)
class ShipCrewControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var crewManagementPort: CrewManagementPort

    private val shipId = UUID.randomUUID()
    private val recruitId = UUID.randomUUID()

    private fun hire(body: String = """{"recruitId": "$recruitId"}""") =
        mockMvc.post("/web/ships/$shipId/crew") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }

    @Test
    fun `POST crew answers 200 with the Crew Member, the Hiring Fee and the Savings as decimal strings and the new Recruit`() {
        val newRecruit = UUID.randomUUID()
        every { crewManagementPort.hire(ShipId(shipId), RecruitId(recruitId)) } returns HireDTO(
            ShipId(shipId), "Salty Whisker", CrewMemberDTO(recruitId, "Mittens", "Strong Paws"),
            Money.of("50.00"), Money.of("950.00"), RecruitDTO(newRecruit, "Whiskers", "Brave", Money.of("50.00"), Portrait.of(newRecruit)),
        )

        hire().andExpect {
            status { isOk() }
            jsonPath("$.shipId") { value(shipId.toString()) }
            jsonPath("$.name") { value("Salty Whisker") }
            jsonPath("$.crewMember.id") { value(recruitId.toString()) }
            jsonPath("$.crewMember.name") { value("Mittens") }
            jsonPath("$.crewMember.trait") { value("Strong Paws") }
            jsonPath("$.hiringFee") { value("50.00") }
            jsonPath("$.savings") { value("950.00") }
            jsonPath("$.newRecruit.id") { value(newRecruit.toString()) }
            jsonPath("$.newRecruit.hiringFee") { value("50.00") }
        }
    }

    @Test
    fun `POST crew answers 404 for a ship not in the fleet`() {
        every { crewManagementPort.hire(any(), any()) } returns null

        hire().andExpect {
            status { isNotFound() }
            content { contentTypeCompatibleWith("application/problem+json") }
        }
    }

    @Test
    fun `POST crew answers 400 without a recruitId`() {
        hire("{}").andExpect { status { isBadRequest() } }
    }

    @Test
    fun `POST crew answers 409 Problem Details for HireRefusedException and SavingsDoNotCoverException`() {
        every { crewManagementPort.hire(any(), any()) } throws HireRefusedException("Old Barnacle is at sea")
        hire().andExpect {
            status { isConflict() }
            content { contentTypeCompatibleWith("application/problem+json") }
            jsonPath("$.title") { value("Hire refused") }
            jsonPath("$.detail") { value("Old Barnacle is at sea") }
        }

        every { crewManagementPort.hire(any(), any()) } throws
            SavingsDoNotCoverException(Money.of("50.00"), "The Savings do not cover the Hiring Fee of 50.00 $")
        hire().andExpect {
            status { isConflict() }
            jsonPath("$.title") { value("Savings do not cover") }
        }
    }

    private val crewMemberId = UUID.randomUUID()

    private fun dismiss() = mockMvc.delete("/web/ships/$shipId/crew/$crewMemberId")

    @Test
    fun `DELETE crew answers 204 after the Dismissal`() {
        every { crewManagementPort.dismiss(ShipId(shipId), CrewMemberId(crewMemberId)) } returns true

        dismiss().andExpect { status { isNoContent() } }
    }

    @Test
    fun `DELETE crew answers 404 for a ship not in the fleet`() {
        every { crewManagementPort.dismiss(any(), any()) } returns false

        dismiss().andExpect {
            status { isNotFound() }
            content { contentTypeCompatibleWith("application/problem+json") }
        }
    }

    @Test
    fun `DELETE crew answers 409 Problem Details for DismissRefusedException`() {
        every { crewManagementPort.dismiss(any(), any()) } throws DismissRefusedException("Old Barnacle is at sea")

        dismiss().andExpect {
            status { isConflict() }
            content { contentTypeCompatibleWith("application/problem+json") }
            jsonPath("$.title") { value("Dismissal refused") }
            jsonPath("$.detail") { value("Old Barnacle is at sea") }
        }
    }
}
