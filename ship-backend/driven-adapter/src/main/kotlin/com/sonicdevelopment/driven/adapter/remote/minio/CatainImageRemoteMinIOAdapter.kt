package com.sonicdevelopment.driven.adapter.remote.minio

import com.sonicdevelopment.domain.model.values.CatainImage
import com.sonicdevelopment.domain.model.values.CatainImageId
import com.sonicdevelopment.domain.ports.driven.CatainImageRemotePort
import org.springframework.stereotype.Service

@Service
class CatainImageRemoteMinIOAdapter(): CatainImageRemotePort {
    override fun findCatainImage(catainImageId: CatainImageId): CatainImage {
        TODO("Not yet implemented")
    }
}