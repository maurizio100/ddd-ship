package com.sonicdevelopment.domain.ports.driven

import com.sonicdevelopment.domain.model.Catain
import com.sonicdevelopment.domain.model.values.CatainId

interface CatainRepository {
    fun findCatainById(catainId: CatainId): Catain?
    fun findAllCatains(): List<Catain>
}