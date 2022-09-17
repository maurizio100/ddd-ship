package at.willhaben.driving.adapter.web

import at.willhaben.domain.ports.driving.ShipCreationDataDTO
import at.willhaben.domain.ports.driving.ShipManagementPort
import at.willhaben.driving.adapter.web.requestmodel.ShipCreationRequest
import at.willhaben.driving.adapter.web.responsemodel.ShipResponse
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/web")
@CrossOrigin(origins = ["http://localhost:4200"])
class ShipController (
        private val shipManagementPort: ShipManagementPort
){

    @PostMapping("/ships")
    fun createShip(@RequestBody ship: ShipCreationRequest): ShipResponse {
        val shipCreationDTO = ShipCreationDataDTO(name = ship.name);
        val createdShip = shipManagementPort.createShip(shipCreationDTO)
        return ShipResponse(
            id = createdShip.id,
            name = createdShip.name
        )
    }
}
