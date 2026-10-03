package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.model.values.EventId
import com.sonicdevelopment.domain.model.values.HarborName
import com.sonicdevelopment.domain.ports.driven.CargoQueryPort
import com.sonicdevelopment.domain.ports.driven.CatainRepository
import com.sonicdevelopment.domain.ports.driven.InboxRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShipRepositoryPort
import com.sonicdevelopment.domain.ports.driven.ShippingOutboxRepository
import com.sonicdevelopment.domain.ports.driven.StockRepositoryPort
import com.sonicdevelopment.domain.ports.driving.shipping.ArrivalManagementPort
import com.sonicdevelopment.domain.ports.driving.shipping.ShippingPublishedDTO
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class ArrivalManagementService(
    private val currentHarbor: HarborName,
    private val inboxRepositoryPort: InboxRepositoryPort,
    private val shipRepositoryPort: ShipRepositoryPort,
    private val catainRepository: CatainRepository,
    private val cargoQueryPort: CargoQueryPort,
    private val stockRepositoryPort: StockRepositoryPort,
    private val shippingOutboxRepository: ShippingOutboxRepository,
) : ArrivalManagementPort {

    @Transactional
    override fun receiveShippingPublished(eventId: EventId, shippingPublished: ShippingPublishedDTO) {
    }
}
