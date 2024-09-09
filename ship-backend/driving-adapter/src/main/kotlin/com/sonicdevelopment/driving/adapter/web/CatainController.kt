package com.sonicdevelopment.driving.adapter.web

import com.sonicdevelopment.domain.model.values.CatainId
import com.sonicdevelopment.domain.ports.driving.catain.CatainImageInformationPort
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController("/catains")
class CatainController(
    private val catainImageInformationPort: CatainImageInformationPort
) {
    @GetMapping("/{catainId}/image")
    fun getCatainImage(@PathVariable("catainId") catainId: Long) {
        catainImageInformationPort.getCatainImage(CatainId(catainId))
    }
}