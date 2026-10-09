package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.ports.driven.PriceRepositoryPort
import com.sonicdevelopment.domain.ports.driven.SavingsRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort
import com.sonicdevelopment.domain.ports.driven.StockRepositoryPort
import com.sonicdevelopment.domain.ports.driving.harbor.IncomingShipManagementPort
import org.springframework.stereotype.Service

@Service
class IncomingShipManagementService(
    private val shipRepositoryPort: ShipRepositoryPort,
    private val priceRepositoryPort: PriceRepositoryPort,
    private val savingsRepositoryPort: SavingsRepositoryPort,
    private val stockRepositoryPort: StockRepositoryPort,
) : IncomingShipManagementPort {

    override fun unloadIncomingShip(shipId: ShipId): Money? = TODO("STORY-045")
}
