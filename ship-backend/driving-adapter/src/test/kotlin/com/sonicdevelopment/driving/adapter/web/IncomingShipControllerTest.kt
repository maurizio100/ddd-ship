package com.sonicdevelopment.driving.adapter.web

import com.ninjasquad.springmockk.MockkBean
import com.sonicdevelopment.domain.model.values.CargoId
import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.ports.driving.cargo.CargoDTO
import com.sonicdevelopment.domain.ports.driving.harbor.HarborInformationPort
import com.sonicdevelopment.domain.ports.driving.harbor.IncomingShipDTO
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.util.*

@WebMvcTest(IncomingShipController::class)
class IncomingShipControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var harborInformationPort: HarborInformationPort

    private val rum = CargoDTO(CargoId(UUID.randomUUID()), "Rum", 5.5F)
    private val sugar = CargoDTO(CargoId(UUID.randomUUID()), "Sugar", 0.7F)

    @Test
    fun `GET web incoming-ships lists each Incoming Ship with its Cargo aboard and the Delivery Price as a decimal string`() {
        val saltyWhisker = UUID.randomUUID()
        every { harborInformationPort.getIncomingShips() } returns listOf(
            IncomingShipDTO(ShipId(saltyWhisker), "Salty Whisker", "Tortuga", listOf(rum, rum, sugar), Money.of("115.00")),
        )

        mockMvc.get("/web/incoming-ships").andExpect {
            status { isOk() }
            jsonPath("$[0].shipId") { value(saltyWhisker.toString()) }
            jsonPath("$[0].name") { value("Salty Whisker") }
            jsonPath("$[0].arrivedFrom") { value("Tortuga") }
            jsonPath("$[0].cargo.length()") { value(3) }
            jsonPath("$[0].cargo[0].id") { value(rum.id.id.toString()) }
            jsonPath("$[0].cargo[1].name") { value("Rum") }
            jsonPath("$[0].cargo[2].name") { value("Sugar") }
            jsonPath("$[0].deliveryPrice") { isString() }
            jsonPath("$[0].deliveryPrice") { value("115.00") }
        }
    }

    @Test
    fun `the Delivery Price is null when a Cargo aboard has no Price`() {
        every { harborInformationPort.getIncomingShips() } returns listOf(
            IncomingShipDTO(ShipId(UUID.randomUUID()), "Salty Whisker", null, listOf(rum), null),
        )

        mockMvc.get("/web/incoming-ships").andExpect {
            status { isOk() }
            jsonPath("$[0].deliveryPrice") { hasJsonPath() }
            jsonPath("$[0].deliveryPrice") { value(null) }
        }
    }
}
