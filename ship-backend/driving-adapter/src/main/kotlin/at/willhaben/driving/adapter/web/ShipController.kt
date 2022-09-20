package at.willhaben.driving.adapter.web

import at.willhaben.domain.ports.driving.*
import at.willhaben.driving.adapter.web.requestmodel.ShipCreationRequest
import at.willhaben.driving.adapter.web.responsemodel.ShipDetailResponse
import at.willhaben.driving.adapter.web.responsemodel.ShipOverviewResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/web/ships")
@CrossOrigin(origins = ["http://localhost:4200"])
class ShipController (
        private val shipManagementPort: ShipManagementPort,
        private val shipInformationPort: ShipInformationPort
){

    @GetMapping
    fun getShips(): List<ShipOverviewResponse> {
        return shipManagementPort.getAllShips().map {toShipResponse(it)}
    }

    @PostMapping
    fun createShip(@RequestBody ship: ShipCreationRequest): ShipOverviewResponse {
        val shipCreationDTO = ShipCreationDataDTO(name = ship.name);
        val createdShip = shipManagementPort.createShip(shipCreationDTO)
        return toShipResponse(createdShip)
    }

    @DeleteMapping("/{shipId}")
    fun deleteShip(@PathVariable("shipId") shipId:Long) {
        shipManagementPort.deleteShip(shipId)
    }

    private fun toShipResponse(ship: ShipDTO) =
        ShipOverviewResponse(
            id = ship.id,
            name = ship.name
        )

    @GetMapping("/{shipId}")
    fun getShip(@PathVariable("shipId") shipId:Long): ShipDetailResponse {
        return shipInformationPort.getShipDetails(shipId)?.let{ toShipDetailResponse(it) }
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource");
    }

    private fun toShipDetailResponse(ship: ShipDetailDTO) =
        ShipDetailResponse(
            id = ship.id,
            name = ship.name
        )
}
