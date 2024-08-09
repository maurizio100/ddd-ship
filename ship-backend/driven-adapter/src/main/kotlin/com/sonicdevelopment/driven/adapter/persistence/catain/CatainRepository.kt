package com.sonicdevelopment.driven.adapter.persistence.catain

import org.springframework.data.jpa.repository.JpaRepository

interface CatainRepository: JpaRepository<CatainPersistenceEntity, Long> {
}