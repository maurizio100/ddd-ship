package com.sonicdevelopment.driven.adapter.persistence.stock

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface StockPersistenceEntityRepository : JpaRepository<StockPersistenceEntity, Long> {

    /**
     * Takes one of the Cargo [cargoId] out of the Stock if it holds any; returns the number of updated
     * rows (1 or 0). The `WHERE` is re-checked after a concurrent writer commits, so the Stock never
     * drops below 0.
     */
    @Modifying
    @Query(
        nativeQuery = true,
        value = "UPDATE stocks SET stock_quantity = stock_quantity - 1 " +
            "WHERE cargo_id = (SELECT id FROM cargos WHERE cargo_id = :cargoId) AND stock_quantity > 0"
    )
    fun takeOne(@Param("cargoId") cargoId: UUID): Int

    /** Adds [quantity] of the Cargo [cargoId] to the Stock, creating its row if it has none. */
    @Modifying
    @Query(
        nativeQuery = true,
        value = "INSERT INTO stocks (id, cargo_id, stock_quantity) " +
            "SELECT nextval('stocks_seq'), id, :quantity FROM cargos WHERE cargo_id = :cargoId " +
            "ON CONFLICT (cargo_id) DO UPDATE SET stock_quantity = stocks.stock_quantity + EXCLUDED.stock_quantity"
    )
    fun add(@Param("cargoId") cargoId: UUID, @Param("quantity") quantity: Int): Int
}
