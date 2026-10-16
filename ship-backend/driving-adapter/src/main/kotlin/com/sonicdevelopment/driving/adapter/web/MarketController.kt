package com.sonicdevelopment.driving.adapter.web

import com.sonicdevelopment.domain.model.values.CargoId
import com.sonicdevelopment.domain.ports.driving.market.MarketPort
import com.sonicdevelopment.driving.adapter.web.requestmodel.PurchaseRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal

@RestController
@CrossOrigin(origins = [])
@RequestMapping("/web/market")
class MarketController(
    private val marketPort: MarketPort
) {

    @PostMapping("/purchases")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun buyCargo(@RequestBody request: PurchaseRequest) {
        val cargoId = request.cargoId ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "cargoId is missing")
        val quantity = wholeQuantityOf(request.quantity)

        marketPort.buyCargo(CargoId(cargoId), quantity)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource")
    }

    private fun wholeQuantityOf(quantity: BigDecimal?): Int {
        // intValueExact refuses a fraction and anything beyond Int.MAX_VALUE
        val whole = quantity?.let { runCatching { it.intValueExact() }.getOrNull() }
        if (whole == null || whole < 1) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity must be a whole number of at least 1")
        }
        return whole
    }
}
