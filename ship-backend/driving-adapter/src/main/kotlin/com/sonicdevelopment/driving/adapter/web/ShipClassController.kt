package com.sonicdevelopment.driving.adapter.web

import com.sonicdevelopment.domain.model.enums.ShipClass
import com.sonicdevelopment.driving.adapter.web.responsemodel.ShipClassOfferResponse
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin(origins = [])
@RequestMapping("/web/ship-classes")
class ShipClassController {
    @GetMapping
    fun getShipClasses(): List<ShipClassOfferResponse> =
        ShipClass.entries.map { ShipClassOfferResponse(it, it.crewCapacity) }
}
