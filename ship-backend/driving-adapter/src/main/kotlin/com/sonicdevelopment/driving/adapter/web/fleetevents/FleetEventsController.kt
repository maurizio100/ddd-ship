package com.sonicdevelopment.driving.adapter.web.fleetevents

import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

/** The fleet-events stream of this Harbor (`text/event-stream`): `ship-arrived` and `ship-left`, after commit. */
@RestController
@CrossOrigin(origins = [])
@RequestMapping("/web/fleet-events")
class FleetEventsController(
    private val fleetEventEmitters: FleetEventEmitters,
) {

    @GetMapping(produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun subscribe(response: HttpServletResponse): SseEmitter {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache")
        response.setHeader("X-Accel-Buffering", "no")
        return fleetEventEmitters.register()
    }
}
