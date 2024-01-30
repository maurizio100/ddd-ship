package com.sonicdevelopment.driven.adapter.persistence.quote

import com.sonicdevelopment.domain.ports.driven.QuoteQueryPort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class QuoteQueryAdapter(
    private val quoteRepository: QuoteRepository
): QuoteQueryPort {
    override fun getQuoteForSailorsCode(sailorsCode: Int): String {
        return quoteRepository.findByIdOrNull(sailorsCode.toLong())?.let {
            it.quote
        } ?: "Yeah go and sail!"
    }
}
