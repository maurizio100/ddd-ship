package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.model.Catain
import com.sonicdevelopment.domain.model.values.CatainId
import com.sonicdevelopment.domain.model.values.CatainImage
import com.sonicdevelopment.domain.ports.driven.CatainImageRemotePort
import com.sonicdevelopment.domain.ports.driven.CatainRepository
import com.sonicdevelopment.domain.ports.driving.catain.CatainImageInformationPort
import org.springframework.stereotype.Service

@Service
class CatainImageService(
    private val catainImageRemotePort: CatainImageRemotePort,
    private val catainRepository: CatainRepository
): CatainImageInformationPort {
    override fun getCatainImage(catainId: CatainId): CatainImage {
        return catainRepository.findCatainById(catainId)?.let {
            fetchCatainImage(it)
        } ?: throw IllegalArgumentException()
    }

    private fun fetchCatainImage(catain: Catain): CatainImage {
        return catainImageRemotePort.findCatainImage(catain.catainImageId)
    }
}