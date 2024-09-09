package com.sonicdevelopment.driven.adapter.persistence.catain

import org.springframework.data.jpa.repository.JpaRepository

interface CatainPersistenceEntityRepository: JpaRepository<CatainPersistenceEntity, Long> {
}