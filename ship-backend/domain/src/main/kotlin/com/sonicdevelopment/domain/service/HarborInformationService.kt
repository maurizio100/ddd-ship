package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.domain.ports.driven.KnownHarborRepositoryPort
import com.sonicdevelopment.domain.ports.driven.PriceRepositoryPort
import com.sonicdevelopment.domain.ports.driven.SavingsRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort
import com.sonicdevelopment.domain.ports.driving.cargo.CargoDTO
import com.sonicdevelopment.domain.ports.driving.harbor.IncomingShipDTO
import com.sonicdevelopment.domain.ports.driving.harbor.HarborInformationPort
import com.sonicdevelopment.domain.ports.driving.harbor.KnownHarborsDTO
import org.springframework.stereotype.Service

@Service
class HarborInformationService(
    private val currentHarbor: HarborName,
    private val knownHarborRepositoryPort: KnownHarborRepositoryPort,
    private val savingsRepositoryPort: SavingsRepositoryPort,
    private val shipRepositoryPort: ShipRepositoryPort,
    private val priceRepositoryPort: PriceRepositoryPort,
) : HarborInformationPort {
    override fun getKnownHarbors(): KnownHarborsDTO =
        KnownHarborsDTO(
            harborName = currentHarbor,
            knownHarbors = knownHarborRepositoryPort.getKnownHarbors()
        )

    override fun getSavings(): Money = savingsRepositoryPort.getSavings()

    /** Delivery Prices are computed when the list is read, from this Harbor's Prices only. */
    override fun getIncomingShips(): List<IncomingShipDTO> {
        val incomingShips = shipRepositoryPort.getAllShips().filter { it.isIncoming }
        if (incomingShips.isEmpty()) return emptyList()
        val prices = priceRepositoryPort.getPrices()
        return incomingShips.map { ship ->
            IncomingShipDTO(
                id = ship.id,
                name = ship.shipName,
                arrivedFrom = ship.arrivedFrom?.name,
                cargo = ship.cargoAboard.map { CargoDTO(it.id, it.name, it.weight) },
                deliveryPrice = DeliveryPrice.of(ship.cargoAboard, prices),
            )
        }
    }
}
