package com.sonicdevelopment.driven.adapter.persistence.quote

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "quotes")
class QuotePersistenceEntity(
    @Id
    @Column(name = "id")
    var id: Long? = null,

    @Column(name = "quote")
    var quote: String
) {
}
