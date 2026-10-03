package com.sonicdevelopment.driven.adapter.persistence.stock

import com.sonicdevelopment.domain.model.values.CargoId
import com.sonicdevelopment.domain.ports.driven.StockRepositoryPort
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class StockRepositoryAdapter(
    private val stockPersistenceEntityRepository: StockPersistenceEntityRepository
) : StockRepositoryPort {

    /** One conditional statement is both the check and the decrement, so concurrent loads cannot oversell. */
    @Transactional(propagation = Propagation.MANDATORY)
    override fun takeOneFromStock(cargoId: CargoId): Boolean =
        stockPersistenceEntityRepository.takeOne(cargoId.id) == 1

    @Transactional(propagation = Propagation.MANDATORY)
    override fun putIntoStock(cargoId: CargoId, quantity: Int) {
        stockPersistenceEntityRepository.add(cargoId.id, quantity)
    }

    override fun getStock(): Map<CargoId, Int> =
        stockPersistenceEntityRepository.findAll().associate { CargoId(it.cargo.cargoId) to it.stockQuantity }
}
