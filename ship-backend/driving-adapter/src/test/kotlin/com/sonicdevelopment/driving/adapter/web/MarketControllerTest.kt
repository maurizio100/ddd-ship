package com.sonicdevelopment.driving.adapter.web

import com.ninjasquad.springmockk.MockkBean
import com.sonicdevelopment.domain.exception.CargoHasNoPriceException
import com.sonicdevelopment.domain.exception.SavingsDoNotCoverException
import com.sonicdevelopment.domain.model.values.CargoId
import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.domain.ports.driving.market.MarketPort
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.post
import java.util.*

@WebMvcTest(MarketController::class)
class MarketControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var marketPort: MarketPort

    private val cargoId = UUID.randomUUID()

    @Test
    fun `POST purchases answers 204 and buys the quantity of the Cargo`() {
        every { marketPort.buyCargo(CargoId(cargoId), 2) } returns Money.of("100.00")

        buy("""{"cargoId":"$cargoId","quantity":2}""").andExpect {
            status { isNoContent() }
        }

        verify(exactly = 1) { marketPort.buyCargo(CargoId(cargoId), 2) }
    }

    @Test
    fun `POST purchases answers 400 when cargoId is missing`() {
        buy("""{"quantity":2}""").andExpect {
            status { isBadRequest() }
            content { contentType(MediaType.APPLICATION_PROBLEM_JSON) }
        }

        verify(exactly = 0) { marketPort.buyCargo(any(), any()) }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "\"quantity\":null,", "\"quantity\":0,", "\"quantity\":-1,", "\"quantity\":1.5,", "\"quantity\":2147483648,"])
    fun `POST purchases answers 400 when the quantity is missing, not a whole number or out of range`(quantity: String) {
        buy("""{$quantity"cargoId":"$cargoId"}""").andExpect {
            status { isBadRequest() }
            content { contentType(MediaType.APPLICATION_PROBLEM_JSON) }
        }

        verify(exactly = 0) { marketPort.buyCargo(any(), any()) }
    }

    @Test
    fun `POST purchases answers 404 Problem Details when the Cargo is unknown`() {
        every { marketPort.buyCargo(CargoId(cargoId), 2) } returns null

        buy("""{"cargoId":"$cargoId","quantity":2}""").andExpect {
            status { isNotFound() }
            content { contentType(MediaType.APPLICATION_PROBLEM_JSON) }
            jsonPath("$.status") { value(404) }
        }
    }

    @Test
    fun `POST purchases answers 409 Problem Details when the Savings do not cover the cost`() {
        every { marketPort.buyCargo(CargoId(cargoId), 2) } throws SavingsDoNotCoverException(Money.of("100.00"))

        buy("""{"cargoId":"$cargoId","quantity":2}""").andExpect {
            status { isConflict() }
            content { contentType(MediaType.APPLICATION_PROBLEM_JSON) }
            jsonPath("$.status") { value(409) }
            jsonPath("$.title") { value("Savings do not cover") }
            jsonPath("$.detail") { value("The Savings do not cover 100.00 $") }
        }
    }

    @Test
    fun `POST purchases answers 409 Problem Details when the Cargo has no Price`() {
        every { marketPort.buyCargo(CargoId(cargoId), 2) } throws CargoHasNoPriceException("Rum has no Price yet")

        buy("""{"cargoId":"$cargoId","quantity":2}""").andExpect {
            status { isConflict() }
            content { contentType(MediaType.APPLICATION_PROBLEM_JSON) }
            jsonPath("$.title") { value("Cargo has no Price") }
            jsonPath("$.detail") { value("Rum has no Price yet") }
        }
    }

    private fun buy(body: String): ResultActionsDsl =
        mockMvc.post("/web/market/purchases") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }
}
