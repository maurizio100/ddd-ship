package com.sonicdevelopment.driven.adapter.persistence.outbox

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ShippingOutboxPersistenceRepository: JpaRepository<ShippingOutboxPersistenceEntity, UUID> {
}