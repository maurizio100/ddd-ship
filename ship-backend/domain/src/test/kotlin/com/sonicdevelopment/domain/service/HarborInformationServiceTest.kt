package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.fixtures.aCargo
import com.sonicdevelopment.domain.model.Cargo
import com.sonicdevelopment.domain.model.Ship
import com.sonicdevelopment.domain.model.values.CatainId
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.domain.ports.driven.KnownHarborRepositoryPort
import com.sonicdevelopment.domain.ports.driven.PriceRepositoryPort
import com.sonicdevelopment.domain.ports.driven.SavingsRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort
import com.sonicdevelopment.domain.ports.driving.harbor.KnownHarborsDTO
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import java.util.*

class HarborInformationServiceTest {

    private val knownHarbors = mockk<KnownHarborRepositoryPort>()
    private val savings = mockk<SavingsRepositoryPort>()
    private val ships = mockk<ShipRepositoryPort>()
    private val prices = mockk<PriceRepositoryPort>()
    private val service = HarborInformationService(HarborName("Tortuga"), knownHarbors, savings, ships, prices)

    private val rum = aCargo(name = "Rum")
    private val sugar = aCargo(name = "Sugar", weight = 0.7F)

    @Test
    fun `returns the current Harbor Name and the Known Harbors`() {
        every { knownHarbors.getKnownHarbors() } returns listOf(HarborName("Nassau"), HarborName("Port Royal"))

        val result = service.getKnownHarbors()

        result shouldBe KnownHarborsDTO(
            harborName = HarborName("Tortuga"),
            knownHarbors = listOf(HarborName("Nassau"), HarborName("Port Royal"))
        )
    }

    @Test
    fun `the Savings are those of the Harbor`() {
        every { savings.getSavings() } returns Money.of("640.50")

        service.getSavings() shouldBe Money.of("640.50")
    }

    @Test
    fun `the Incoming Ships are listed with the Delivery Price from this Harbor's Prices, each Cargo counted as often as it is aboard`() {
        val saltyWhisker = aShip("Salty Whisker", cargoAboard = listOf(rum, rum, sugar), incoming = true, arrivedFrom = "Nassau")
        every { ships.getAllShips() } returns listOf(
            aShip("Black Pearl"),
            // Cargo aboard but not Incoming: refused by its own Home Harbor (STORY-028)
            aShip("Refused Rover", cargoAboard = listOf(rum), incoming = false),
            saltyWhisker,
        )
        every { prices.getPrices() } returns mapOf(rum.id to Money.of("40.00"), sugar.id to Money.of("35.00"))

        val incoming = service.getIncomingShips()

        incoming.map { it.name } shouldBe listOf("Salty Whisker")
        val listed = incoming.single()
        listed.id shouldBe saltyWhisker.id
        listed.arrivedFrom shouldBe "Nassau"
        listed.cargo.map { it.name } shouldBe listOf("Rum", "Rum", "Sugar")
        listed.cargo.map { it.id } shouldBe listOf(rum.id, rum.id, sugar.id)
        listed.deliveryPrice shouldBe Money.of("115.00")
    }

    @Test
    fun `the Delivery Price is null when a Cargo aboard has no Price`() {
        every { ships.getAllShips() } returns listOf(aShip("Salty Whisker", cargoAboard = listOf(rum, sugar), incoming = true))
        every { prices.getPrices() } returns mapOf(rum.id to Money.of("40.00"))

        service.getIncomingShips().single().deliveryPrice shouldBe null
    }

    private fun aShip(
        name: String,
        cargoAboard: List<Cargo> = emptyList(),
        incoming: Boolean = false,
        arrivedFrom: String? = null,
    ) = Ship(
        name = name,
        catainId = CatainId(UUID.randomUUID()),
        catainName = "Furry Jones",
        homeHarbor = HarborName("Port Royal"),
        arrivedFrom = arrivedFrom?.let { HarborName(it) },
        cargoAboard = cargoAboard,
        incoming = incoming,
    )
}
