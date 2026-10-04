package com.sonicdevelopment.driving.adapter.web

import com.ninjasquad.springmockk.MockkBean
import com.sonicdevelopment.domain.model.values.CargoId
import com.sonicdevelopment.domain.ports.driving.cargo.AvailableCargoDTO
import com.sonicdevelopment.domain.ports.driving.cargo.CargoInformationPort
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.util.*

@WebMvcTest(CargoController::class)
class CargoControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var cargoInformationPort: CargoInformationPort

    @Test
    fun `GET web cargos returns the Available Cargo with its Stock`() {
        val rumId = UUID.randomUUID()
        every { cargoInformationPort.getAvailableCargo() } returns listOf(
            AvailableCargoDTO(id = CargoId(rumId), name = "Rum", weight = 5.5F, stock = 2)
        )

        mockMvc.get("/web/cargos").andExpect {
            status { isOk() }
            content { json("""[{"id":"$rumId","name":"Rum","weight":5.5,"stock":2}]""", true) }
        }
    }
}
