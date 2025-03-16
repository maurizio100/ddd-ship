package com.sonicdevelopment.domain.ports.driven

import com.sonicdevelopment.domain.model.values.SailorsCode
import com.sonicdevelopment.domain.model.values.ShippingQuote

interface QuoteRepositoryPort {
    fun getQuoteForSailorsCode(sailorsCode: SailorsCode): ShippingQuote
}
