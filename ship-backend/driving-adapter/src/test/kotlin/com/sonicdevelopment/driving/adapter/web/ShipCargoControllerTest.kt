package com.sonicdevelopment.driving.adapter.web

import com.ninjasquad.springmockk.MockkBean
import com.sonicdevelopment.domain.exception.CargoOutOfStockException
import com.sonicdevelopment.domain.exception.ShipTooHeavyException
import com.sonicdevelopment.domain.model.values.CargoId
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.ports.driving.cargo.CargoLoadManagementPort
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.post
import java.util.*

@WebMvcTest(ShipCargoController::class)
class ShipCargoControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var cargoLoadManagementPort: CargoLoadManagementPort

    private val shipId = UUID.randomUUID()
    private val cargoId = UUID.randomUUID()

    @Test
    fun `POST cargos answers 409 Problem Details when the Cargo is out of Stock`() {
        every { cargoLoadManagementPort.addCargo(ShipId(shipId), CargoId(cargoId)) } throws
            CargoOutOfStockException("Silk is out of Stock")

        loadCargo("""{"cargoId":"$cargoId"}""").andExpect {
            status { isConflict() }
            content { contentType(MediaType.APPLICATION_PROBLEM_JSON) }
            jsonPath("$.status") { value(409) }
            jsonPath("$.title") { value("Cargo out of Stock") }
            jsonPath("$.detail") { value("Silk is out of Stock") }
        }
    }

    @Test
    fun `POST cargos answers 409 Problem Details when the ship would exceed its Max Weight`() {
        every { cargoLoadManagementPort.addCargo(ShipId(shipId), CargoId(cargoId)) } throws
            ShipTooHeavyException("Loading Rum would exceed the Max Weight of 15.0")

        loadCargo("""{"cargoId":"$cargoId"}""").andExpect {
            status { isConflict() }
            content { contentType(MediaType.APPLICATION_PROBLEM_JSON) }
            jsonPath("$.title") { value("Ship too heavy") }
            jsonPath("$.detail") { value("Loading Rum would exceed the Max Weight of 15.0") }
        }
    }

    @Test
    fun `POST cargos answers 400 when cargoId is missing`() {
        loadCargo("""{}""").andExpect {
            status { isBadRequest() }
            content { contentType(MediaType.APPLICATION_PROBLEM_JSON) }
        }

        verify(exactly = 0) { cargoLoadManagementPort.addCargo(any(), any()) }
    }

    @Test
    fun `POST cargos answers 404 Problem Details when the ship is unknown`() {
        every { cargoLoadManagementPort.addCargo(ShipId(shipId), CargoId(cargoId)) } returns null

        loadCargo("""{"cargoId":"$cargoId"}""").andExpect {
            status { isNotFound() }
            content { contentType(MediaType.APPLICATION_PROBLEM_JSON) }
            jsonPath("$.status") { value(404) }
        }
    }

    private fun loadCargo(body: String): ResultActionsDsl =
        mockMvc.post("/web/ships/$shipId/cargos") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }
}
