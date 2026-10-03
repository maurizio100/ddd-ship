package com.sonicdevelopment.driving.adapter.web

import com.ninjasquad.springmockk.MockkBean
import com.sonicdevelopment.domain.exception.ShippingNotPreparingException
import com.sonicdevelopment.domain.exception.UnknownHarborException
import com.sonicdevelopment.domain.model.enums.ShippingState
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.model.values.ShippingId
import com.sonicdevelopment.domain.model.values.ShippingQuote
import com.sonicdevelopment.domain.ports.driving.shipping.ShippingDetailsDTO
import com.sonicdevelopment.domain.ports.driving.shipping.ShippingInformationPort
import com.sonicdevelopment.domain.ports.driving.shipping.ShippingManagementPort
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.put
import java.util.*

@WebMvcTest(ShipShippingController::class)
class ShipShippingControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var shippingManagementPort: ShippingManagementPort

    @MockkBean
    lateinit var shippingInformationPort: ShippingInformationPort

    private val shipId = UUID.randomUUID()

    @Test
    fun `PUT releases to the Destination Harbor from the body`() {
        every { shippingManagementPort.releaseShipping(ShipId(shipId), HarborName("Port Royal")) } returns
            aReleasedShipping(HarborName("Port Royal"))

        release("""{"destinationHarbor":"Port Royal"}""").andExpect {
            status { isOk() }
            jsonPath("$.shipId") { value(shipId.toString()) }
            jsonPath("$.destinationHarbor") { value("Port Royal") }
        }

        verify(exactly = 1) { shippingManagementPort.releaseShipping(ShipId(shipId), HarborName("Port Royal")) }
    }

    @ParameterizedTest
    @ValueSource(strings = ["""{}""", """{"destinationHarbor":null}""", """{"destinationHarbor":"  "}"""])
    fun `missing or blank destinationHarbor is 400`(body: String) {
        release(body).andExpect {
            status { isBadRequest() }
            content { contentType(MediaType.APPLICATION_PROBLEM_JSON) }
            jsonPath("$.detail") { value("A Destination Harbor is required") }
        }

        verify(exactly = 0) { shippingManagementPort.releaseShipping(any(), any()) }
    }

    @Test
    fun `UnknownHarborException is 409 Problem Details with title Unknown Destination Harbor`() {
        every { shippingManagementPort.releaseShipping(ShipId(shipId), HarborName("Atlantis")) } throws
            UnknownHarborException("Atlantis is not a Known Harbor")

        release("""{"destinationHarbor":"Atlantis"}""").andExpect {
            status { isConflict() }
            content { contentType(MediaType.APPLICATION_PROBLEM_JSON) }
            jsonPath("$.status") { value(409) }
            jsonPath("$.title") { value("Unknown Destination Harbor") }
            jsonPath("$.detail") { value("Atlantis is not a Known Harbor") }
        }
    }

    @Test
    fun `ShippingNotPreparingException is 409 Problem Details with title Ship not being prepared`() {
        every { shippingManagementPort.releaseShipping(ShipId(shipId), HarborName("Nassau")) } throws
            ShippingNotPreparingException("Black Pearl is not being prepared")

        release("""{"destinationHarbor":"Nassau"}""").andExpect {
            status { isConflict() }
            content { contentType(MediaType.APPLICATION_PROBLEM_JSON) }
            jsonPath("$.status") { value(409) }
            jsonPath("$.title") { value("Ship not being prepared") }
            jsonPath("$.detail") { value("Black Pearl is not being prepared") }
        }
    }

    @Test
    fun `PUT answers 404 Problem Details when the ship is unknown`() {
        every { shippingManagementPort.releaseShipping(ShipId(shipId), HarborName("Port Royal")) } returns null

        release("""{"destinationHarbor":"Port Royal"}""").andExpect {
            status { isNotFound() }
            content { contentType(MediaType.APPLICATION_PROBLEM_JSON) }
        }
    }

    private fun release(body: String): ResultActionsDsl =
        mockMvc.put("/web/ships/$shipId/shippings") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }

    private fun aReleasedShipping(destinationHarbor: HarborName) =
        ShippingDetailsDTO(
            shipId = ShipId(shipId),
            shippingId = ShippingId(UUID.randomUUID()),
            catainId = UUID.randomUUID(),
            catainName = "Furry Jones",
            shipName = "Black Pearl",
            shippingState = ShippingState.SHIPPING,
            shippingQuote = ShippingQuote("Fair winds"),
            cargo = emptyList(),
            actualWeight = 0.0F,
            destinationHarbor = destinationHarbor,
        )
}
