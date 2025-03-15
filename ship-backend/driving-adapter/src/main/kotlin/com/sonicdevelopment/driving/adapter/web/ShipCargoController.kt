package com.sonicdevelopment.driving.adapter.web

import com.sonicdevelopment.domain.model.values.CargoId
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.ports.driving.cargo.CargoLoadManagementPort
import com.sonicdevelopment.driving.adapter.web.mapper.toShipDetailResponse
import com.sonicdevelopment.driving.adapter.web.requestmodel.CargoLoadRequest
import com.sonicdevelopment.driving.adapter.web.responsemodel.ShipDetailResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*

@RestController
@CrossOrigin(origins = [])
@RequestMapping("/web/ships")
class ShipCargoController(
    private val cargoLoadManagementPort: CargoLoadManagementPort
) {

    @PostMapping("/{shipId}/cargos")
    fun addCargo(@PathVariable("shipId") shipId: UUID, @RequestBody cargoLoad: CargoLoadRequest): ShipDetailResponse {
        val cargoId = cargoLoad.cargoId ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource")
        val updatedShip = cargoLoadManagementPort.addCargo(ShipId(shipId), CargoId(cargoId))
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource")

        return toShipDetailResponse(updatedShip)
    }

    @DeleteMapping("/{shipId}/cargos/{cargoId}")
    fun addCargo(@PathVariable("shipId") shipId: UUID, @PathVariable("cargoId") cargoId: UUID): ShipDetailResponse {
        val updatedShip = cargoLoadManagementPort.removeCargo(ShipId(shipId), CargoId(cargoId))
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource")

        return toShipDetailResponse(updatedShip)
    }
}