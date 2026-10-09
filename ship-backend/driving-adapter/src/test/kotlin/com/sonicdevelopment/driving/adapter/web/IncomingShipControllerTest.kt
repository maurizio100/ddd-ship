package com.sonicdevelopment.driving.adapter.web

import com.ninjasquad.springmockk.MockkBean
import com.sonicdevelopment.domain.exception.SavingsDoNotCoverException
import com.sonicdevelopment.domain.exception.ShipNotIncomingException
import com.sonicdevelopment.domain.model.values.CargoId
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.ports.driving.cargo.CargoDTO
import com.sonicdevelopment.domain.ports.driving.harbor.HarborInformationPort
import com.sonicdevelopment.domain.ports.driving.harbor.IncomingShipDTO
import com.sonicdevelopment.domain.ports.driving.harbor.IncomingShipManagementPort
import com.sonicdevelopment.domain.ports.driving.harbor.RefusalDTO
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.util.*

@WebMvcTest(IncomingShipController::class)
class IncomingShipControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var harborInformationPort: HarborInformationPort

    @MockkBean
    lateinit var incomingShipManagementPort: IncomingShipManagementPort

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

    @Test
    fun `POST unloading unloads the Incoming Ship and answers 204`() {
        val saltyWhisker = UUID.randomUUID()
        every { incomingShipManagementPort.unloadIncomingShip(ShipId(saltyWhisker)) } returns Money.of("115.00")

        mockMvc.post("/web/incoming-ships/$saltyWhisker/unloading").andExpect {
            status { isNoContent() }
            content { string("") }
        }
    }

    @Test
    fun `POST unloading a ship not in the fleet answers 404 as Problem Details`() {
        every { incomingShipManagementPort.unloadIncomingShip(any()) } returns null

        mockMvc.post("/web/incoming-ships/${UUID.randomUUID()}/unloading").andExpect {
            status { isNotFound() }
            content { contentTypeCompatibleWith("application/problem+json") }
            jsonPath("$.status") { value(404) }
        }
    }

    @Test
    fun `POST unloading the Savings cannot cover answers 409 with the Delivery Price`() {
        every { incomingShipManagementPort.unloadIncomingShip(any()) } throws
            SavingsDoNotCoverException(Money.of("115.00"), "The Savings do not cover the Delivery Price of 115.00 $")

        mockMvc.post("/web/incoming-ships/${UUID.randomUUID()}/unloading").andExpect {
            status { isConflict() }
            content { contentTypeCompatibleWith("application/problem+json") }
            jsonPath("$.title") { value("Savings do not cover") }
            jsonPath("$.detail") { value("The Savings do not cover the Delivery Price of 115.00 $") }
        }
    }

    @Test
    fun `POST unloading a ship that is not Incoming answers 409`() {
        every { incomingShipManagementPort.unloadIncomingShip(any()) } throws
            ShipNotIncomingException("Salty Whisker is not an Incoming Ship")

        mockMvc.post("/web/incoming-ships/${UUID.randomUUID()}/unloading").andExpect {
            status { isConflict() }
            jsonPath("$.title") { value("Not an Incoming Ship") }
            jsonPath("$.detail") { value("Salty Whisker is not an Incoming Ship") }
        }
    }

    @Test
    fun `POST refusal that sends the ship home answers 204`() {
        val saltyWhisker = UUID.randomUUID()
        every { incomingShipManagementPort.refuseIncomingShip(ShipId(saltyWhisker)) } returns
            RefusalDTO(HarborName("Port Royal"))

        mockMvc.post("/web/incoming-ships/$saltyWhisker/refusal").andExpect {
            status { isNoContent() }
            content { string("") }
        }
    }

    @Test
    fun `POST refusal at the Home Harbor that keeps the ship answers 204`() {
        val saltyWhisker = UUID.randomUUID()
        every { incomingShipManagementPort.refuseIncomingShip(ShipId(saltyWhisker)) } returns RefusalDTO(null)

        mockMvc.post("/web/incoming-ships/$saltyWhisker/refusal").andExpect {
            status { isNoContent() }
            content { string("") }
        }
    }

    @Test
    fun `POST refusal of a ship not in the fleet answers 404 as Problem Details`() {
        every { incomingShipManagementPort.refuseIncomingShip(any()) } returns null

        mockMvc.post("/web/incoming-ships/${UUID.randomUUID()}/refusal").andExpect {
            status { isNotFound() }
            content { contentTypeCompatibleWith("application/problem+json") }
            jsonPath("$.status") { value(404) }
        }
    }

    @Test
    fun `POST refusal of a ship that is not Incoming answers 409`() {
        every { incomingShipManagementPort.refuseIncomingShip(any()) } throws
            ShipNotIncomingException("Salty Whisker is not an Incoming Ship")

        mockMvc.post("/web/incoming-ships/${UUID.randomUUID()}/refusal").andExpect {
            status { isConflict() }
            content { contentTypeCompatibleWith("application/problem+json") }
            jsonPath("$.title") { value("Not an Incoming Ship") }
            jsonPath("$.detail") { value("Salty Whisker is not an Incoming Ship") }
        }
    }
}
