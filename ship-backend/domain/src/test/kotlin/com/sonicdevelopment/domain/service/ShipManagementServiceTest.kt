package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.model.Catain
import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.values.CatainId
import com.sonicdevelopment.domain.model.values.CatainImageId
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.ports.driven.CatainRepository
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort.InitialShipInformation
import com.sonicdevelopment.domain.ports.driving.ship.ShipCreationDataDTO
import com.sonicdevelopment.domain.ports.driving.ship.ShipUpdateDataDTO
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import java.util.*

class ShipManagementServiceTest {

    private val ships = mockk<ShipRepositoryPort>()
    private val catains = mockk<CatainRepository>()
    private val service = ShipManagementService(ships, catains)

    private val whiskers = Catain(CatainId(UUID.randomUUID()), "Whiskers", CatainImageId("whiskers"))

    @Test
    fun `a ship registered by the User has no Origin Harbor, and renaming keeps it`() {
        val saved = mutableListOf<InitialShipInformation>()
        every { ships.saveNewShip(capture(saved)) } returns Unit
        every { catains.findCatainById(whiskers.catainId) } returns whiskers

        service.createShip(ShipCreationDataDTO(name = "Interceptor", catainId = whiskers.catainId))

        saved.single().arrivedFrom shouldBe null

        val arrived = Ship(
            name = "Black Pearl",
            catainId = whiskers.catainId,
            catainName = whiskers.catainName,
            arrivedFrom = HarborName("Tortuga"),
        )
        every { ships.getShipDetails(arrived.id) } returns arrived

        val renamed = service.updateShip(arrived.id, ShipUpdateDataDTO(name = "Wicked Wench"))

        saved.last().shipName shouldBe "Wicked Wench"
        saved.last().arrivedFrom shouldBe HarborName("Tortuga")
        renamed!!.arrivedFrom shouldBe "Tortuga"
    }
}
