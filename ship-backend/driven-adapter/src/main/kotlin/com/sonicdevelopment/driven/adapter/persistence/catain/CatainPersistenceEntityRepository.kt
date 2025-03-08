package com.sonicdevelopment.driven.adapter.persistence.catain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CatainPersistenceEntityRepository: JpaRepository<CatainPersistenceEntity, Long> {
    fun findByCatainId(catainId: UUID):CatainPersistenceEntity?
}