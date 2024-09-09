package com.sonicdevelopment.domain.ports.driven

import com.sonicdevelopment.domain.model.values.CatainImage
import com.sonicdevelopment.domain.model.values.CatainImageId

interface CatainImageRemotePort {
    fun findCatainImage(catainImageId: CatainImageId): CatainImage
}