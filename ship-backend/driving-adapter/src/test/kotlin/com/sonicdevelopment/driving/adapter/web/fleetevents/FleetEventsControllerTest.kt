package com.sonicdevelopment.driving.adapter.web.fleetevents

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(FleetEventsController::class)
@Import(FleetEventEmitters::class)
class FleetEventsControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var fleetEventEmitters: FleetEventEmitters

    @Test
    fun `GET web fleet-events opens an unbuffered event stream`() {
        val before = fleetEventEmitters.count()

        mockMvc.get("/web/fleet-events") { accept = MediaType.TEXT_EVENT_STREAM }.andExpect {
            request { asyncStarted() }
            status { isOk() }
            header {
                string("Content-Type", org.hamcrest.Matchers.startsWith(MediaType.TEXT_EVENT_STREAM_VALUE))
                string("Cache-Control", "no-cache")
                string("X-Accel-Buffering", "no")
            }
        }

        fleetEventEmitters.count() shouldBe before + 1
    }
}
