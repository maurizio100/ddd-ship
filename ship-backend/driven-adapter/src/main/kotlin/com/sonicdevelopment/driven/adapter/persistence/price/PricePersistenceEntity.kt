package com.sonicdevelopment.driven.adapter.persistence.price

import com.sonicdevelopment.driven.adapter.persistence.cargo.CargoPersistenceEntity
import jakarta.persistence.*
import java.math.BigDecimal

/** One Cargo's Price at this Harbor. Read-only for JPA: writes go through the native query. */
@Entity
@Table(name = "prices")
class PricePersistenceEntity(
    @Id
    @GeneratedValue
    @Column(name = "id")
    var id: Long,

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cargo_id", nullable = false)
    var cargo: CargoPersistenceEntity,

    @Column(name = "price_amount")
    var priceAmount: BigDecimal
)
