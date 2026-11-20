package com.sonicdevelopment.driving.adapter.web

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(ShipClassController::class)
class ShipClassControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `GET web ship-classes returns the Ship Classes with their Crew Capacity as JSON`() {
        mockMvc.get("/web/ship-classes").andExpect {
            status { isOk() }
            content { json("""[{"shipClass":"SLOOP","crewCapacity":2},{"shipClass":"BRIG","crewCapacity":4},{"shipClass":"GALLEON","crewCapacity":6}]""", true) }
        }
    }
}
