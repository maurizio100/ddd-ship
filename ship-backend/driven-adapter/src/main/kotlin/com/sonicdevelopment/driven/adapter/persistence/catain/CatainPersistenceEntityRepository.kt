package com.sonicdevelopment.driven.adapter.persistence.catain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CatainPersistenceEntityRepository: JpaRepository<CatainPersistenceEntity, Long> {
}