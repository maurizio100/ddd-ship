package com.sonicdevelopment.driving.adapter.web

import com.sonicdevelopment.domain.model.values.CatainId
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.ports.driven.CatainRepository
import com.sonicdevelopment.domain.ports.driving.ship.*
import com.sonicdevelopment.driving.adapter.web.mapper.toShipDetailResponse
import com.sonicdevelopment.driving.adapter.web.mapper.toShipResponse
import com.sonicdevelopment.driving.adapter.web.requestmodel.ShipCreationRequest
import com.sonicdevelopment.driving.adapter.web.requestmodel.ShipUpdateRequest
import com.sonicdevelopment.driving.adapter.web.responsemodel.ShipDetailResponse
import com.sonicdevelopment.driving.adapter.web.responsemodel.ShipOverviewResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@CrossOrigin(origins = [])
@RequestMapping("/web/ships")
class ShipController (
    private val shipManagementPort: ShipManagementPort,
    private val shipInformationPort: ShipInformationPort,
){

    @GetMapping
    fun getShips(): List<ShipOverviewResponse> {
        return shipInformationPort.getAllShips().map {toShipResponse(it)}
    }

    @GetMapping("/{shipId}")
    fun getShip(@PathVariable("shipId") shipId:UUID): ShipDetailResponse {
        return shipInformationPort.getShipDetails(ShipId(shipId))?.let{ toShipDetailResponse(it) }
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource")
    }

    @PostMapping
    fun createShip(@RequestBody ship: ShipCreationRequest): ShipOverviewResponse {
        if (ship.catainId == null) throw IllegalArgumentException("No catainid given")
        val catainId = CatainId(ship.catainId)

        val shipCreationDTO = ShipCreationDataDTO(name = ship.name, catainId = catainId)
        val createdShip = shipManagementPort.createShip(shipCreationDTO)
        return toShipResponse(createdShip)
    }

    @PutMapping("/{shipId}")
    fun updateShip(@PathVariable("shipId") shipId: UUID, @RequestBody ship: ShipUpdateRequest): ShipOverviewResponse {
        val shipUpdateData = ShipUpdateDataDTO(name = ship.name)
        return shipManagementPort.updateShip(ShipId(shipId), shipUpdateData)?.let {
            toShipResponse(it)
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource")
    }

    @DeleteMapping("/{shipId}")
    fun deleteShip(@PathVariable("shipId") shipId:UUID) {
        shipManagementPort.deleteShip(ShipId(shipId))
    }
}
