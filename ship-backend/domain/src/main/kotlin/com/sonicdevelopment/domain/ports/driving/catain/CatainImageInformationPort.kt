package com.sonicdevelopment.domain.ports.driving.catain

import com.sonicdevelopment.domain.model.values.CatainId
import com.sonicdevelopment.domain.model.values.CatainImage

interface CatainImageInformationPort {
    fun getCatainImage(catainId: CatainId): CatainImage
}