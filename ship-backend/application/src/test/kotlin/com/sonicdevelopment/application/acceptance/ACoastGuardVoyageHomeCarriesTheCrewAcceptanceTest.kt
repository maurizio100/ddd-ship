package com.sonicdevelopment.application.acceptance

import com.sonicdevelopment.application.acceptance.fixtures.FakeDrivenPorts
import com.sonicdevelopment.application.acceptance.fixtures.FakeHarborTest
import com.sonicdevelopment.application.acceptance.fixtures.SeedData
import com.sonicdevelopment.application.acceptance.fixtures.aShipImprisonedRecord
import com.sonicdevelopment.application.acceptance.fixtures.aShippingPublishedRecord
import com.sonicdevelopment.application.acceptance.fixtures.givenKnownHarbors
import com.sonicdevelopment.domain.model.CrewMember
import com.sonicdevelopment.domain.model.values.CrewMemberId
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.driving.adapter.messaging.CoastGuardEventListener
import com.sonicdevelopment.driving.adapter.messaging.ShippingEventListener
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource
import java.util.*

/** A ship the Coast Guard sends home imprisoned sails with its Crew: Shipping Published carries it. */
@FakeHarborTest
@TestPropertySource(properties = ["harbor.name=Port Royal"])
class ACoastGuardVoyageHomeCarriesTheCrewAcceptanceTest {

    @Autowired
    lateinit var fakes: FakeDrivenPorts

    @Autowired
    lateinit var shippingEventListener: ShippingEventListener

    @Autowired
    lateinit var coastGuardEventListener: CoastGuardEventListener

    private val saltyWhiskerId = UUID.randomUUID()

    @BeforeEach
    fun aFreshHarbor() {
        fakes.reset()
        fakes.givenKnownHarbors("Tortuga")
    }

    @Test
    fun `A ship sent home imprisoned carries its Crew`() {
        shippingEventListener.onShippingEvent(
            aShippingPublishedRecord(
                shipId = saltyWhiskerId, shipName = "Salty Whisker", catainId = SeedData.aCatainId,
                cargoIds = emptyList(), originHarbor = "Tortuga", destinationHarbor = "Port Royal", homeHarbor = "Tortuga",
            )
        )
        val mittens = CrewMember(CrewMemberId(UUID.randomUUID()), "Mittens", "Strong Paws")
        val patch = CrewMember(CrewMemberId(UUID.randomUUID()), "Patch", "Sharp Eyes")
        fakes.fleet.addCrewMember(ShipId(saltyWhiskerId), mittens)
        fakes.fleet.addCrewMember(ShipId(saltyWhiskerId), patch)

        coastGuardEventListener.onCoastGuardEvent(
            aShipImprisonedRecord(
                shipId = saltyWhiskerId, shipName = "Salty Whisker", holdingHarbor = "Port Royal", homeHarbor = "Tortuga",
            )
        )

        val published = fakes.outbox.shippingPublished().single()
        published.imprisoned shouldBe true
        published.crew.map { Triple(it.id, it.name, it.trait) } shouldBe listOf(
            Triple(mittens.id.id, "Mittens", "Strong Paws"), Triple(patch.id.id, "Patch", "Sharp Eyes"),
        )
    }
}
