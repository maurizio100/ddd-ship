package com.sonicdevelopment.driven.adapter.persistence.arrival

import com.sonicdevelopment.domain.model.values.ShipId
import com.sonicdevelopment.domain.model.values.ShippingId
import com.sonicdevelopment.domain.ports.driven.ArrivalRepositoryPort
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class ArrivalRepositoryAdapter(
    private val arrivalPersistenceEntityRepository: ArrivalPersistenceEntityRepository
) : ArrivalRepositoryPort {

    /** One statement is both the check and the record, so concurrent re-publications cannot both win. */
    @Transactional(propagation = Propagation.MANDATORY)
    override fun recordArrival(shippingId: ShippingId, shipId: ShipId): Boolean =
        arrivalPersistenceEntityRepository.insertIfAbsent(shippingId.id, shipId.id) == 1
}
