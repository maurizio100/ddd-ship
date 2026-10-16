package com.sonicdevelopment.driven.adapter.persistence.price

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.math.BigDecimal
import java.util.*

interface PricePersistenceEntityRepository : JpaRepository<PricePersistenceEntity, Long> {

    /**
     * Stores [amount] as the Price of the Cargo [cargoId] unless it has one; returns the number of inserted
     * rows (1 or 0). The first Price wins, also against a concurrent writer.
     */
    @Modifying
    @Query(
        nativeQuery = true,
        value = "INSERT INTO prices (id, cargo_id, price_amount) " +
            "SELECT nextval('prices_seq'), id, :amount FROM cargos WHERE cargo_id = :cargoId " +
            "ON CONFLICT (cargo_id) DO NOTHING"
    )
    fun remember(@Param("cargoId") cargoId: UUID, @Param("amount") amount: BigDecimal): Int
}
