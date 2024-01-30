package com.sonicdevelopment.driven.adapter.persistence.cargo

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CargoRepository: JpaRepository<CargoPersistenceEntity, Long> {
}
