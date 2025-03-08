package com.sonicdevelopment.driving.adapter.web

import com.sonicdevelopment.domain.model.Catain
import com.sonicdevelopment.domain.model.values.CatainId
import com.sonicdevelopment.domain.ports.driving.catain.CatainImageInformationPort
import com.sonicdevelopment.domain.ports.driving.catain.CatainInformationPort
import com.sonicdevelopment.driving.adapter.web.responsemodel.CatainResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/catains")
class CatainController(
    private val catainImageInformationPort: CatainImageInformationPort,
    private val catainInformationPort: CatainInformationPort
) {

    @GetMapping
    fun getAllCatains(): List<CatainResponse> {
        return catainInformationPort.getAllCatains().map {
            toCatainResponse(it)
        }
    }

    private fun toCatainResponse(catain: Catain): CatainResponse {
        return CatainResponse(
            id = catain.catainId.id,
            name = catain.catainName
        )
    }

    @GetMapping("/{catainId}/image")
    fun getCatainImage(@PathVariable("catainId") catainId: UUID): ResponseEntity<ByteArray> {
        val catainImage = catainImageInformationPort.getCatainImage(CatainId(catainId))
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$catainId\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)  // Set appropriate content type
            .body(catainImage.image)
    }
}