package com.sonicdevelopment.driven.adapter.remote.minio

import com.sonicdevelopment.domain.model.values.CatainImage
import com.sonicdevelopment.domain.model.values.CatainImageId
import com.sonicdevelopment.domain.ports.driven.CatainImageRemotePort
import io.minio.GetObjectArgs
import io.minio.MinioClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream

@Service
class CatainImageRemoteMinIOAdapter(
    @Value("\${minio.bucket.name}") private val bucket: String?,
    private val minioClient: MinioClient
): CatainImageRemotePort {
    override fun findCatainImage(catainImageId: CatainImageId): CatainImage {
        val imageResponse = minioClient.getObject(
            GetObjectArgs.builder()
                .bucket(bucket)
                .`object`(catainImageId.id)
                .build()
        )

        // Convert InputStream to byte array
        val byteArrayOutputStream = ByteArrayOutputStream()
        val buffer = ByteArray(4096)
        var bytesRead: Int

        while ((imageResponse.read(buffer).also { bytesRead = it }) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead)
        }
        return CatainImage(byteArrayOutputStream.toByteArray())
    }
}