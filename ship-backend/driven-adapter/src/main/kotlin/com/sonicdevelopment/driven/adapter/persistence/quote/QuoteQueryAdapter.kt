package com.sonicdevelopment.driven.adapter.persistence.quote

import com.sonicdevelopment.domain.model.values.SailorsCode
import com.sonicdevelopment.domain.model.values.ShippingQuote
import com.sonicdevelopment.domain.ports.driven.QuoteRepositoryPort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class QuoteQueryAdapter(
    private val quoteRepository: QuoteRepository
): QuoteRepositoryPort {
    override fun getQuoteForSailorsCode(sailorsCode: SailorsCode): ShippingQuote {
        return quoteRepository.findByIdOrNull(
            sailorsCode.codeValue.toLong()
        )?.let {
            ShippingQuote(it.quote)
        } ?: ShippingQuote("Yeah go and sail!")
    }
}
