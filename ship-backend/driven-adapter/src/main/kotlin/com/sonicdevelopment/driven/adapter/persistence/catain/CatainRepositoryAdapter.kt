package com.sonicdevelopment.driven.adapter.persistence.catain

import com.sonicdevelopment.domain.model.Catain
import com.sonicdevelopment.domain.model.values.CatainId
import com.sonicdevelopment.domain.model.values.CatainImageId
import com.sonicdevelopment.domain.ports.driven.CatainRepository
import org.springframework.stereotype.Service

@Service
class CatainRepositoryAdapter(
    private val catainPersistenceEntityRepository: CatainPersistenceEntityRepository
) : CatainRepository {
    override fun findCatainById(catainId: CatainId): Catain? {
        return catainPersistenceEntityRepository.findByCatainId(
            catainId.id
        )?.let{ toCatain(it) }
    }

    override fun findAllCatains(): List<Catain> {
        return catainPersistenceEntityRepository.findAll().map {
            toCatain(it)
        }
    }

    private fun toCatain(catainPersistenceEntity: CatainPersistenceEntity): Catain {
        return Catain(
            catainId = CatainId(catainPersistenceEntity.catainId),
            catainName = catainPersistenceEntity.catainName,
            catainImageId = CatainImageId(catainPersistenceEntity.catainImageId)
        )
    }
}