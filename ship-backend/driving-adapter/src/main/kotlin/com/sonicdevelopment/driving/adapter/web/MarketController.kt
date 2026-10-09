package com.sonicdevelopment.driving.adapter.web

import com.sonicdevelopment.domain.ports.driving.market.MarketPort
import com.sonicdevelopment.driving.adapter.web.requestmodel.PurchaseRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin(origins = [])
@RequestMapping("/web/market")
class MarketController(
    private val marketPort: MarketPort
) {

    @PostMapping("/purchases")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun buyCargo(@RequestBody request: PurchaseRequest) {
        TODO("STORY-025")
    }
}
