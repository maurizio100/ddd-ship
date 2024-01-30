package com.sonicdevelopment.driven.adapter.persistence.ship

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ShipRepository: JpaRepository<ShipPersistenceEntity, Long> {
}
