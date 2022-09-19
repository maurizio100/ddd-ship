package at.willhaben.driving.adapter.web

import at.willhaben.domain.ports.driving.ShipCreationDataDTO
import at.willhaben.domain.ports.driving.ShipDTO
import at.willhaben.domain.ports.driving.ShipManagementPort
import at.willhaben.driving.adapter.web.requestmodel.ShipCreationRequest
import at.willhaben.driving.adapter.web.responsemodel.ShipOverviewResponse
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/web/ships")
@CrossOrigin(origins = ["http://localhost:4200"])
class ShipController (
        private val shipManagementPort: ShipManagementPort
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

    private fun toShipResponse(ship: ShipDTO) =
        ShipOverviewResponse(
            id = ship.id,
            name = ship.name
        )
}
