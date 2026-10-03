package com.sonicdevelopment.driven.adapter.persistence.stock

import com.sonicdevelopment.driven.adapter.persistence.cargo.CargoPersistenceEntity
import jakarta.persistence.*

/** One Cargo's entry in the Harbor's Stock. Read-only for JPA: writes go through the native queries. */
@Entity
@Table(name = "stocks")
class StockPersistenceEntity(
    @Id
    @GeneratedValue
    @Column(name = "id")
    var id: Long,

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cargo_id", nullable = false)
    var cargo: CargoPersistenceEntity,

    @Column(name = "stock_quantity")
    var stockQuantity: Int
)
