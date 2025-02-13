package com.sonicdevelopment.domain.model.values

data class CatainId(private val id: Long? = 1L) {
    fun id(): Long { return id ?: 1L }
}
