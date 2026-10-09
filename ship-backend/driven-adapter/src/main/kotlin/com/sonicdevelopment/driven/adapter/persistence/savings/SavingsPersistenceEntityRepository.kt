package com.sonicdevelopment.driven.adapter.persistence.savings

import org.springframework.data.jpa.repository.JpaRepository

interface SavingsPersistenceEntityRepository : JpaRepository<SavingsPersistenceEntity, Long>
