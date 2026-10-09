package com.sonicdevelopment.driving.adapter.web

import com.sonicdevelopment.domain.ports.driving.cargo.CargoInformationPort
import com.sonicdevelopment.driving.adapter.web.responsemodel.StockedCargoResponse
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin(origins = [])
@RequestMapping("/web/stock")
class StockController(
    private val cargoInformationPort: CargoInformationPort
) {
    @GetMapping
    fun getStock(): List<StockedCargoResponse> =
        cargoInformationPort.getStockOverview().map {
            StockedCargoResponse(cargoId = it.id.id, name = it.name, quantity = it.quantity, price = null)
        }
}
