package com.sonicdevelopment.driving.adapter.web

import com.ninjasquad.springmockk.MockkBean
import com.sonicdevelopment.domain.model.values.CargoId
import com.sonicdevelopment.domain.model.values.Money
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
            StockedCargoDTO(id = CargoId(rumId), name = "Rum", quantity = 0, price = Money.dollars(42)),
            StockedCargoDTO(id = CargoId(aleId), name = "Ale", quantity = 3, price = Money.of("57.5"))
        )

        mockMvc.get("/web/stock").andExpect {
            status { isOk() }
            content {
                json(
                    """[{"cargoId":"$rumId","name":"Rum","quantity":0,"price":"42.00"},{"cargoId":"$aleId","name":"Ale","quantity":3,"price":"57.50"}]""",
                    true
                )
            }
        }
    }

    @Test
    fun `the Price is a decimal string, not a number`() {
        every { cargoInformationPort.getStockOverview() } returns listOf(
            StockedCargoDTO(id = CargoId(UUID.randomUUID()), name = "Rum", quantity = 3, price = Money.dollars(42))
        )

        mockMvc.get("/web/stock").andExpect {
            status { isOk() }
            jsonPath("$[0].price") { isString() }
            jsonPath("$[0].price") { value("42.00") }
        }
    }

    @Test
    fun `a Cargo without a Price has a null price`() {
        every { cargoInformationPort.getStockOverview() } returns listOf(
            StockedCargoDTO(id = CargoId(UUID.randomUUID()), name = "Rum", quantity = 3, price = null)
        )

        mockMvc.get("/web/stock").andExpect {
            status { isOk() }
            content { json("""[{"name":"Rum","price":null}]""", false) }
        }
    }
}
