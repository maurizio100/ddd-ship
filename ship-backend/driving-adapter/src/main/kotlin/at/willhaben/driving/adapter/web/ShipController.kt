package at.willhaben.driving.adapter.web

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/web")
@CrossOrigin(origins = ["http://localhost:4200"])
class ShipController (
    @Value("\${api.hello.value}") val hellValue: String
){

    @PostMapping("/ships")
    fun createShip(@RequestBody ship: Ship): Ship {
        return ship.copy(id = 40)
    }

    data class Ship(
        val id: Long?,
        val name: String
    )
}
