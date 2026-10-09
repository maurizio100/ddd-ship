package com.sonicdevelopment.driving.adapter.web

import com.ninjasquad.springmockk.MockkBean
import com.sonicdevelopment.domain.model.enums.ShippingState
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.ports.driving.ship.ShipDTO
import com.sonicdevelopment.domain.ports.driving.ship.ShipDetailDTO
import com.sonicdevelopment.domain.ports.driving.ship.ShipInformationPort
import com.sonicdevelopment.domain.ports.driving.ship.ShipManagementPort
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.util.*

@WebMvcTest(ShipController::class)
class ShipControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var shipInformationPort: ShipInformationPort

    @MockkBean
    lateinit var shipManagementPort: ShipManagementPort

    private val blackPearl = ShipId(UUID.randomUUID())
    private val interceptor = ShipId(UUID.randomUUID())

    @Test
    fun `the ship list and ship details carry arrivedFrom, null when none`() {
        every { shipInformationPort.getAllShips() } returns listOf(
            ShipDTO(blackPearl, "Black Pearl", "Whiskers", ShippingState.IDLE, arrivedFrom = "Tortuga", homeHarbor = "Port Royal"),
            ShipDTO(interceptor, "Interceptor", "Whiskers", ShippingState.IDLE, arrivedFrom = null, homeHarbor = "Tortuga"),
        )
        every { shipInformationPort.getShipDetails(blackPearl) } returns aShipDetail(blackPearl, "Tortuga")
        every { shipInformationPort.getShipDetails(interceptor) } returns aShipDetail(interceptor, null)

        mockMvc.get("/web/ships").andExpect {
            status { isOk() }
            jsonPath("$[0].arrivedFrom") { value("Tortuga") }
            jsonPath("$[1].arrivedFrom") { hasJsonPath() }
            jsonPath("$[1].arrivedFrom") { value(null) }
        }
        mockMvc.get("/web/ships/${blackPearl.id}").andExpect {
            status { isOk() }
            jsonPath("$.arrivedFrom") { value("Tortuga") }
        }
        mockMvc.get("/web/ships/${interceptor.id}").andExpect {
            status { isOk() }
            jsonPath("$.arrivedFrom") { hasJsonPath() }
            jsonPath("$.arrivedFrom") { value(null) }
        }
    }

    @Test
    fun `the ships overview carries whether a ship is an Incoming Ship`() {
        every { shipInformationPort.getAllShips() } returns listOf(
            ShipDTO(blackPearl, "Black Pearl", "Whiskers", ShippingState.IDLE, arrivedFrom = "Tortuga", incoming = true, homeHarbor = "Port Royal"),
            ShipDTO(interceptor, "Interceptor", "Whiskers", ShippingState.IDLE, homeHarbor = "Tortuga"),
        )

        mockMvc.get("/web/ships").andExpect {
            status { isOk() }
            jsonPath("$[0].incoming") { value(true) }
            jsonPath("$[1].incoming") { value(false) }
        }
    }

    @Test
    fun `the ship list and ship details carry the Home Harbor`() {
        every { shipInformationPort.getAllShips() } returns listOf(
            ShipDTO(blackPearl, "Black Pearl", "Whiskers", ShippingState.IDLE, arrivedFrom = "Tortuga", homeHarbor = "Port Royal"),
            ShipDTO(interceptor, "Interceptor", "Whiskers", ShippingState.IDLE, homeHarbor = "Tortuga"),
        )
        every { shipInformationPort.getShipDetails(blackPearl) } returns aShipDetail(blackPearl, "Tortuga", homeHarbor = "Port Royal")

        mockMvc.get("/web/ships").andExpect {
            status { isOk() }
            jsonPath("$[0].homeHarbor") { value("Port Royal") }
            jsonPath("$[1].homeHarbor") { value("Tortuga") }
        }
        mockMvc.get("/web/ships/${blackPearl.id}").andExpect {
            status { isOk() }
            jsonPath("$.homeHarbor") { value("Port Royal") }
        }
    }

    private fun aShipDetail(id: ShipId, arrivedFrom: String?, homeHarbor: String = "Tortuga") =
        ShipDetailDTO(id, "Black Pearl", emptyList(), 0.0F, 15.0F, arrivedFrom = arrivedFrom, homeHarbor = homeHarbor)
}
