package com.sonicdevelopment.domain.ports.driving.catain

import com.sonicdevelopment.domain.model.Catain
import com.sonicdevelopment.domain.model.values.CatainId
import com.sonicdevelopment.domain.model.values.CatainImage

interface CatainInformationPort {
    fun getAllCatains(): List<Catain>
}