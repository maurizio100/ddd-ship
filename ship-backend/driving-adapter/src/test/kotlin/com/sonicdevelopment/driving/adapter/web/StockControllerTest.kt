package com.sonicdevelopment.driving.adapter.web

import com.ninjasquad.springmockk.MockkBean
import com.sonicdevelopment.domain.model.values.CargoId
import com.sonicdevelopment.domain.ports.driving.cargo.CargoInformationPort
import com.sonicdevelopment.domain.ports.driving.cargo.StockedCargoDTO
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.util.*

@WebMvcTest(StockController::class)
class StockControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var cargoInformationPort: CargoInformationPort

    @Test
    fun `GET web stock returns the catalog with quantities`() {
        val rumId = UUID.randomUUID()
        val aleId = UUID.randomUUID()
        every { cargoInformationPort.getStockOverview() } returns listOf(
            StockedCargoDTO(id = CargoId(rumId), name = "Rum", quantity = 0),
            StockedCargoDTO(id = CargoId(aleId), name = "Ale", quantity = 3)
        )

        mockMvc.get("/web/stock").andExpect {
            status { isOk() }
            content {
                json(
                    """[{"cargoId":"$rumId","name":"Rum","quantity":0},{"cargoId":"$aleId","name":"Ale","quantity":3}]""",
                    true
                )
            }
        }
    }
}
