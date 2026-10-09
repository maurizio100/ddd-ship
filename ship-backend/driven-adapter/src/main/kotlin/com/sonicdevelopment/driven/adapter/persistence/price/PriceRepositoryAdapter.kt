package com.sonicdevelopment.driven.adapter.persistence.price

import com.sonicdevelopment.domain.model.values.CargoId
import com.sonicdevelopment.domain.model.values.Money
import com.sonicdevelopment.domain.ports.driven.PriceRepositoryPort
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class PriceRepositoryAdapter(
    private val pricePersistenceEntityRepository: PricePersistenceEntityRepository
) : PriceRepositoryPort {

    override fun getPrices(): Map<CargoId, Money> = TODO("STORY-024")

    @Transactional(propagation = Propagation.MANDATORY)
    override fun rememberPrice(cargoId: CargoId, price: Money): Boolean = TODO("STORY-024")
}
