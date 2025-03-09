package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.model.Catain
import com.sonicdevelopment.domain.ports.driven.CatainRepository
import com.sonicdevelopment.domain.ports.driving.catain.CatainInformationPort
import org.springframework.stereotype.Service

@Service
class CatainInformationService(
    val catainRepository: CatainRepository
) :CatainInformationPort {
    override fun getAllCatains(): List<Catain> {
        return catainRepository.findAllCatains()
    }
}